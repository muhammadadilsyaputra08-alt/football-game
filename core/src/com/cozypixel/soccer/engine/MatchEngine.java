package com.cozypixel.soccer.engine;

import com.badlogic.gdx.math.Vector2;
import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mesin pertandingan sepak bola 2D top-down (arcade). Murni logika — tanpa OpenGL — sehingga
 * bisa diuji headless (lihat {@link MatchSimulator}). Dunia = piksel virtual 640x384, y ke atas.
 * Tuan rumah (tim 0, dikendalikan pemain) menyerang ke kanan; tamu (tim 1) menyerang ke kiri.
 */
public final class MatchEngine {

    // ---- geometri lapangan ----
    public static final float GL = 44f, GR = 596f;      // garis gawang kiri / kanan
    public static final float TB = 58f, TT = 330f;      // garis samping bawah / atas
    public static final float CX = 320f, CY = 194f;
    public static final float GOAL_HALF = 30f;          // setengah lebar mulut gawang
    public static final float NET_DEPTH = 26f;
    public static final float EXTRA_LEN = 30f;

    public enum Difficulty {
        EASY("Mudah", 0.92f, 0.75f), NORMAL("Normal", 1.0f, 1.0f), HARD("Sulit", 1.06f, 1.30f);
        public final String label; public final float speed, skill;
        Difficulty(String l, float sp, float sk) { label = l; speed = sp; skill = sk; }
    }

    public enum Phase { KICKOFF, PLAYING, GOAL, HALFTIME, FINISHED }

    public enum Ev { WHISTLE, KICK, SHOT, PASS, SAVE, BLOCK, GOAL, TACKLE, TACKLE_FAIL, OUT, HALFTIME, FULLTIME, EXTRA, PENALTIES }

    public interface Listener { void onEvent(Ev ev, MatchPlayer p, float x, float y, String text); }

    public static final class TeamStats {
        public int shots, onTarget, passes, tackles, saves, corners;
        public float possession;
    }

    private enum Kick { PASS, SHOT, CLEAR }

    // ---- state publik (dibaca renderer) ----
    public final MatchConfig cfg;
    public final MatchPlayer[][] squad = new MatchPlayer[2][];
    public final List<MatchPlayer> all = new ArrayList<>(22);
    public final String[] names = new String[2];
    public final int[] score = new int[2];
    public final TeamStats[] stats = {new TeamStats(), new TeamStats()};
    public final Vector2 ballPos = new Vector2(CX, CY);
    public final Vector2 ballVel = new Vector2();
    public MatchPlayer carrier;
    public MatchPlayer lastTouch;
    public int lastTouchTeam = -1;
    public boolean ballIsShot;
    public int shotTeam = -1;
    public Phase phase = Phase.KICKOFF;
    public float phaseTimer;
    public float clock, extraClock;
    public int half = 1;
    public boolean extra;
    public final float matchLen;
    public final HumanInput input = new HumanInput();
    public MatchPlayer controlled;
    public final MatchResult result = new MatchResult();
    public int scoringTeam = -1;
    public float netAnim;      // >0 = animasi jaring (sisi di scoringTeam)
    public float timeSinceStart;
    public boolean autoplay;   // true = semua pemain dikendalikan AI (simulasi/uji)

    private final Difficulty diff;
    private final Random rnd;
    private Listener listener;
    private final MatchPlayer[][] chaser = new MatchPlayer[2][2];
    private float switchTimer;

    public MatchEngine(MatchConfig cfg) {
        this.cfg = cfg;
        this.rnd = new Random(cfg.seed);
        this.diff = cfg.difficulty;
        this.matchLen = cfg.matchSeconds;
        build(0, cfg.home);
        build(1, cfg.away);
        names[0] = cfg.home.name;
        names[1] = cfg.away.name;
        result.homeName = names[0];
        result.awayName = names[1];
        resetKickoff(0);
    }

    public void setListener(Listener l) { listener = l; }

    private void emit(Ev ev, MatchPlayer p, float x, float y, String text) {
        if (listener != null) listener.onEvent(ev, p, x, y, text);
    }

    // =====================================================================
    //  Pembentukan tim
    // =====================================================================

    static String spriteKey(Player p, boolean away) {
        String id = p.id == null ? "" : p.id;
        String key;
        switch (id) {
            case "CB1": key = "Characters/Default_Players/CB/Player_CB_01"; break;
            case "CB2": key = "Characters/Default_Players/CB/Player_CB_02"; break;
            case "GK": case "RB": case "LB": case "CDM": case "CM": case "CAM": case "RW": case "ST": case "LW":
                key = "Characters/Default_Players/" + id + "/Player_" + id; break;
            case "Leon": case "Rafael": case "Kai":
                key = "Characters/Recruitable_Players/" + id + "/Player_" + id; break;
            default: {
                String pos = p.position == null ? "CM" : p.position;
                key = pos.equals("CB") ? "Characters/Default_Players/CB/Player_CB_01"
                        : "Characters/Default_Players/" + pos + "/Player_" + pos;
            }
        }
        return away ? key + "_away" : key;
    }

    private void build(int team, TeamSpec spec) {
        List<Formations.Slot> slots = Formations.get(spec.formation);
        Player[] a = Formations.assign(spec.xi, slots);
        squad[team] = new MatchPlayer[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            Formations.Slot s = slots.get(i);
            Player pm = a[i];
            if (pm == null) { // cadangan darurat bila XI kurang dari 11
                pm = new Player("fill" + team + i, "Cadangan", s.role);
                pm.number = 20 + i;
            }
            float hx = team == 0 ? s.x : 1f - s.x;
            float hy = team == 0 ? s.y : 1f - s.y;
            MatchPlayer mp = new MatchPlayer(team, i, s.role, pm, spriteKey(pm, spec.away), hx, hy);
            squad[team][i] = mp;
            all.add(mp);
        }
    }

    public float teamOvr(int t) {
        float s = 0f;
        for (MatchPlayer p : squad[t]) s += p.model.getOverall();
        return s / squad[t].length;
    }

    // =====================================================================
    //  Util
    // =====================================================================

    static float dirOf(int team) { return team == 0 ? 1f : -1f; }
    static float goalX(int team) { return team == 0 ? GR : GL; }       // gawang yang diserang
    static float ownGoalX(int team) { return team == 0 ? GL : GR; }
    private float skillOf(int team) { return team == 1 ? diff.skill : 1f; }
    private float speedOf(int team) { return team == 1 ? diff.speed : 1f; }
    private static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }

    private float worldX(int team, float xn) { return GL + (team == 0 ? xn : 1f - xn) * (GR - GL); }

    private float homeWorldX(MatchPlayer p) { return GL + p.homeX * (GR - GL); }
    private float homeWorldY(MatchPlayer p) { return TB + p.homeY * (TT - TB); }

    public int minute() {
        if (extra) return 90 + (int) (extraClock / EXTRA_LEN * 30f);
        return Math.min(90, (int) (clock / matchLen * 90f));
    }

    public boolean isHumanCarrier() { return carrier != null && carrier == controlled; }

    // =====================================================================
    //  Kickoff / reset
    // =====================================================================

    public void resetKickoff(int kickTeam) {
        carrier = null;
        ballVel.setZero();
        ballIsShot = false;
        ballPos.set(CX, CY);
        netAnim = 0;
        for (int t = 0; t < 2; t++) {
            for (MatchPlayer p : squad[t]) {
                float x = homeWorldX(p), y = homeWorldY(p);
                if (p.gk) x = ownGoalX(t) + dirOf(t) * 14f;
                else if (t == 0) x = Math.min(x, CX - 12f - (p.index % 3) * 5f);
                else x = Math.max(x, CX + 12f + (p.index % 3) * 5f);
                p.pos.set(x, y);
                p.vel.setZero();
                p.desired.setZero();
                p.face.set(dirOf(t), 0);
                p.stun = p.tackleTimer = p.kickTimer = p.hold = p.diveTimer = p.celebrate = 0;
                p.noPickup = 0; p.tackleCd = 0; p.fallen = false; p.charging = false;
                p.tx = x; p.ty = y; p.thinkTimer = rnd.nextFloat() * 0.2f;
                p.shotSeenAt = -1f;
            }
        }
        MatchPlayer taker = null;
        for (MatchPlayer p : squad[kickTeam]) if (p.role.equals("ST")) { taker = p; break; }
        if (taker == null) taker = squad[kickTeam][squad[kickTeam].length - 1];
        taker.pos.set(CX - dirOf(kickTeam) * 8f, CY);
        taker.face.set(dirOf(kickTeam), 0);
        carrier = taker;
        ballPos.set(taker.pos).mulAdd(taker.face, 7f);
        lastTouch = taker;
        lastTouchTeam = kickTeam;
        controlled = kickTeam == 0 ? taker : nearestOutfield(0, CX, CY);
        phase = Phase.KICKOFF;
        phaseTimer = 1.4f;
        emit(Ev.WHISTLE, null, CX, CY, "Kick off");
    }

    private MatchPlayer nearestOutfield(int team, float x, float y) {
        MatchPlayer best = null; float bd = 1e9f;
        for (MatchPlayer p : squad[team]) {
            if (p.gk) continue;
            float d = Vector2.dst2(p.pos.x, p.pos.y, x, y);
            if (d < bd) { bd = d; best = p; }
        }
        return best;
    }

    // =====================================================================
    //  Loop utama
    // =====================================================================

    public void update(float dt) {
        if (phase == Phase.FINISHED) return;
        timeSinceStart += dt;
        if (netAnim > 0) netAnim -= dt;
        for (MatchPlayer p : all) tickTimers(p, dt);

        switch (phase) {
            case KICKOFF:
                phaseTimer -= dt;
                boolean human = carrier != null && carrier.team == 0;
                boolean act = input.passPressed || input.shootPressed || input.throughPressed || input.move.len2() > 0.25f;
                if (!(human && act)) input.clearEdges();
                if (phaseTimer <= 0f || (human && act && phaseTimer < 1.0f)) {
                    phase = Phase.PLAYING;
                    emit(Ev.WHISTLE, null, CX, CY, "");
                }
                break;
            case PLAYING:
                stepPlay(dt);
                break;
            case GOAL:
                stepGoal(dt);
                break;
            case HALFTIME:
                phaseTimer -= dt;
                if (phaseTimer <= 0f) { half = 2; resetKickoff(1); }
                break;
            default:
                break;
        }
    }

    private void tickTimers(MatchPlayer p, float dt) {
        p.noPickup -= dt; p.kickTimer -= dt; p.tackleTimer -= dt; p.tackleCd -= dt;
        p.stun -= dt; p.hold -= dt; p.diveTimer -= dt; p.celebrate -= dt; p.noSave -= dt;
        if (p.shotSeenAt > 0) p.shotSeenAt -= dt;
        p.actionTime += dt;
        if (p.stun <= 0) p.fallen = false;
    }

    // =====================================================================
    //  Fase bermain
    // =====================================================================

    private void stepPlay(float dt) {
        if (autoplay) controlled = null; else autoSwitch(dt);
        humanInput(dt);
        computeChasers();
        aiThink(dt);
        movePlayers(dt);
        updateBall(dt);
        goalkeeperSaves();
        pickups();
        checkBoundsAndGoal();
        if (phase != Phase.PLAYING) return;

        if (carrier != null) stats[carrier.team].possession += dt;
        else if (lastTouchTeam >= 0) stats[lastTouchTeam].possession += dt;

        if (!extra) {
            clock += dt;
            if (half == 1 && clock >= matchLen / 2f) { startHalftime(); return; }
            if (clock >= matchLen) { endRegulation(); }
        } else {
            extraClock += dt;
            if (extraClock >= EXTRA_LEN) shootout();
        }
    }

    private void startHalftime() {
        phase = Phase.HALFTIME;
        phaseTimer = 2.8f;
        carrier = null;
        ballVel.setZero();
        for (MatchPlayer p : all) p.desired.setZero();
        emit(Ev.HALFTIME, null, CX, CY, "Turun minum");
    }

    private void endRegulation() {
        if (score[0] != score[1] || cfg.allowDraw) { finish(); return; }
        extra = true;
        emit(Ev.EXTRA, null, CX, CY, "Perpanjangan waktu - gol emas!");
        resetKickoff(rnd.nextInt(2));
    }

    private void shootout() {
        float diffOvr = teamOvr(0) - teamOvr(1);
        float p0 = clamp(0.72f + diffOvr / 250f, 0.55f, 0.88f);
        float p1 = clamp(0.72f - diffOvr / 250f, 0.55f, 0.88f);
        int a = 0, b = 0;
        for (int i = 0; i < 5; i++) { if (rnd.nextFloat() < p0) a++; if (rnd.nextFloat() < p1) b++; }
        int guard = 0;
        while (a == b && guard++ < 20) { if (rnd.nextFloat() < p0) a++; if (rnd.nextFloat() < p1) b++; }
        if (a == b) a++;
        result.penHome = a; result.penAway = b;
        emit(Ev.PENALTIES, null, CX, CY, "Adu penalti " + a + " - " + b);
        finish();
    }

    private void finish() {
        phase = Phase.FINISHED;
        result.homeGoals = score[0];
        result.awayGoals = score[1];
        result.extraTime = extra;
        for (int i = 0; i < 2; i++) {
            result.stats[i].shots = stats[i].shots; result.stats[i].onTarget = stats[i].onTarget;
            result.stats[i].passes = stats[i].passes; result.stats[i].tackles = stats[i].tackles;
            result.stats[i].saves = stats[i].saves; result.stats[i].corners = stats[i].corners;
            result.stats[i].possession = stats[i].possession;
        }
        emit(Ev.FULLTIME, null, CX, CY, "Peluit panjang");
    }

    private void stepGoal(float dt) {
        phaseTimer -= dt;
        for (MatchPlayer p : all) {
            p.desired.setZero();
            if (p.celebrate <= 0f) p.wantSprint = false;
        }
        movePlayers(dt);
        // bola menggelinding di dalam jaring
        ballPos.mulAdd(ballVel, dt);
        ballVel.scl(Math.max(0f, 1f - 3f * dt));
        float gx = scoringTeam == 0 ? GR : GL;
        float in = scoringTeam == 0 ? 1f : -1f;
        float back = gx + in * (NET_DEPTH - 4f);
        if ((ballPos.x - back) * in > 0) { ballPos.x = back; ballVel.x *= -0.25f; }
        if (Math.abs(ballPos.y - CY) > GOAL_HALF - 3f) {
            ballPos.y = CY + Math.signum(ballPos.y - CY) * (GOAL_HALF - 3f); ballVel.y *= -0.3f;
        }
        if (phaseTimer <= 0f) {
            if (extra) { finish(); return; }
            if (!extra && clock >= matchLen && score[0] == score[1] && !cfg.allowDraw) { endRegulation(); return; }
            if (clock >= matchLen) { finish(); return; }
            if (half == 1 && clock >= matchLen / 2f) { startHalftime(); return; }
            resetKickoff(1 - scoringTeam);
        }
    }

    // =====================================================================
    //  Input manusia
    // =====================================================================

    private void autoSwitch(float dt) {
        switchTimer -= dt;
        if (controlled == null) controlled = nearestOutfield(0, ballPos.x, ballPos.y);
        if (carrier != null && carrier.team == 0) {
            if (carrier != controlled && !(controlled != null && controlled.charging)) controlled = carrier;
            return;
        }
        if (switchTimer > 0f) return;
        switchTimer = 0.18f;
        float rx = carrier != null ? carrier.pos.x : ballPos.x;
        float ry = carrier != null ? carrier.pos.y : ballPos.y;
        MatchPlayer best = nearestOutfield(0, rx, ry);
        if (best == null) return;
        if (controlled.gk) { controlled = best; return; }
        float dc = controlled.pos.dst(rx, ry), db = best.pos.dst(rx, ry);
        if (best != controlled && dc - db > 14f) controlled = best;
    }

    private void manualSwitch() {
        float rx = carrier != null ? carrier.pos.x : ballPos.x;
        float ry = carrier != null ? carrier.pos.y : ballPos.y;
        MatchPlayer best = null; float bd = 1e9f;
        for (MatchPlayer p : squad[0]) {
            if (p.gk || p == controlled) continue;
            float d = p.pos.dst2(rx, ry);
            if (d < bd) { bd = d; best = p; }
        }
        if (best != null) { controlled = best; switchTimer = 0.6f; }
    }

    private boolean canKickBall(MatchPlayer p) {
        if (carrier == p) return p.hold < 0.65f;
        if (carrier != null) return false;
        return p.pos.dst(ballPos) < 15f && ballVel.len() < 320f && p.noPickup <= 0f && p.stun <= 0f;
    }

    private void humanInput(float dt) {
        MatchPlayer p = controlled;
        HumanInput in = input;
        if (p == null) { in.clearEdges(); return; }
        if (p.gk && carrier == p && p.hold < -1.5f) gkDistribute(p); // jangan menahan bola selamanya
        if (p.stun > 0f || (p.gk && carrier == p) || (carrier == p && p.hold > 0f)) {
            p.desired.setZero(); p.wantSprint = false;
        } else {
            p.desired.set(in.move);
            if (p.desired.len2() > 1f) p.desired.nor();
            p.wantSprint = in.sprint && p.desired.len2() > 0.04f;
        }
        p.targetSpeed = 1f;

        if (in.switchPressed) manualSwitch();
        if (in.tacklePressed && !p.gk) tryTackle(p);

        if (in.shootPressed && canKickBall(p) && p.stun <= 0f) { p.charging = true; p.charge = 0f; }
        if (p.charging) {
            p.charge += dt;
            if (!in.shootHeld || p.charge > 0.85f) {
                float power = 0.42f + 0.58f * clamp(p.charge / 0.75f, 0f, 1f);
                p.charging = false;
                if (canKickBall(p)) humanShoot(p, power);
            }
        }
        if (in.passPressed && canKickBall(p)) humanPass(p, false);
        if (in.throughPressed && canKickBall(p)) humanPass(p, true);
        in.clearEdges();
    }

    private Vector2 aimDir(MatchPlayer p) {
        Vector2 a = new Vector2(input.move);
        if (a.len2() < 0.04f) a.set(p.face);
        return a.nor();
    }

    private void humanShoot(MatchPlayer p, float power) {
        if (p.gk) { clearBall(p, aimDir(p)); return; }
        Vector2 d = aimDir(p);
        float gx = goalX(p.team);
        float dx = gx - p.pos.x;
        if (d.x * dirOf(p.team) > 0.2f && Math.abs(dx) > 1f) {
            float yAt = p.pos.y + d.y / d.x * (gx - p.pos.x);
            float off = yAt - CY;
            if (Math.abs(off) < GOAL_HALF + 22f) {
                float aimY = CY + clamp(off, -(GOAL_HALF - 6f), GOAL_HALF - 6f);
                d.set(gx - p.pos.x, aimY - p.pos.y).nor();
            }
        }
        kick(p, d.x, d.y, shotSpeed(p, power), Kick.SHOT, (1f - p.shoot / 125f) * 0.12f * (0.6f + 0.8f * power));
    }

    private float shotSpeed(MatchPlayer p, float power) { return 230f + 150f * power + p.shoot * 0.6f; }

    private void humanPass(MatchPlayer p, boolean through) {
        if (p.gk) { // kiper: umpan pendek ke pemain terdekat searah, tendangan jauh jika tak ada
            MatchPlayer t = findPassTarget(p, aimDir(p), 0.45f, false);
            if (t != null) passTo(p, t, false); else clearBall(p, aimDir(p));
            return;
        }
        Vector2 aim = aimDir(p);
        MatchPlayer t = findPassTarget(p, aim, through ? 0.35f : 0.55f, through);
        if (t != null) passTo(p, t, through);
        else kick(p, aim.x, aim.y, through ? 260f : 215f, Kick.PASS, 0.05f);
    }

    private MatchPlayer findPassTarget(MatchPlayer p, Vector2 aim, float minCos, boolean through) {
        MatchPlayer best = null; float bs = -1e9f;
        for (MatchPlayer q : squad[p.team]) {
            if (q == p) continue;
            float dx = q.pos.x - p.pos.x, dy = q.pos.y - p.pos.y;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < 22f || d > 330f) continue;
            float cos = (dx * aim.x + dy * aim.y) / d;
            if (cos < minCos) continue;
            float s = cos * 2f - d / 260f + Math.min(nearestOppDist(q.pos.x, q.pos.y, q.team), 60f) / 80f;
            if (through) s += ((q.pos.x - p.pos.x) * dirOf(p.team)) / 200f;
            if (laneBlocked(p.pos.x, p.pos.y, q.pos.x, q.pos.y, p.team)) s -= 0.8f;
            if (q.gk) s -= 0.6f;
            if (s > bs) { bs = s; best = q; }
        }
        return best;
    }

    // =====================================================================
    //  Aksi bola
    // =====================================================================

    private void kick(MatchPlayer p, float dx, float dy, float speed, Kick type, float dev) {
        Vector2 d = new Vector2(dx, dy);
        if (d.len2() < 1e-6f) d.set(p.face);
        d.nor();
        float sk = Math.max(0.5f, skillOf(p.team));
        d.rotateRad((float) rnd.nextGaussian() * dev / sk);
        if (carrier == p) carrier = null;
        ballPos.set(p.pos).mulAdd(d, 10f);
        ballVel.set(d).scl(speed);
        p.noPickup = 0.30f;
        p.kickTimer = 0.24f;
        p.actionTime = 0f;
        p.kickWasShot = type == Kick.SHOT;
        p.face.set(d);
        p.hold = 0f;
        lastTouch = p;
        lastTouchTeam = p.team;
        ballIsShot = type == Kick.SHOT;
        shotTeam = p.team;
        TeamStats st = stats[p.team];
        if (type == Kick.SHOT) {
            st.shots++;
            float gx = goalX(p.team);
            if (d.x * dirOf(p.team) > 0.05f) {
                float t = (gx - p.pos.x) / d.x;
                float yAt = p.pos.y + d.y * t;
                if (Math.abs(yAt - CY) < GOAL_HALF) st.onTarget++;
            }
            emit(Ev.SHOT, p, ballPos.x, ballPos.y, "");
        } else if (type == Kick.PASS) {
            st.passes++;
            emit(Ev.PASS, p, ballPos.x, ballPos.y, "");
        } else {
            emit(Ev.KICK, p, ballPos.x, ballPos.y, "");
        }
    }

    private void passTo(MatchPlayer p, MatchPlayer tgt, boolean through) {
        float lead = through ? 0.55f : 0.30f;
        float tx = tgt.pos.x + tgt.vel.x * lead + (through ? dirOf(p.team) * 30f : 0f);
        float ty = tgt.pos.y + tgt.vel.y * lead;
        tx = clamp(tx, GL + 6f, GR - 6f);
        ty = clamp(ty, TB + 6f, TT - 6f);
        float dx = tx - p.pos.x, dy = ty - p.pos.y;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = clamp(105f + 1.15f * d, 150f, 400f);
        float skillStat = p.gk ? p.distribution : p.pass;
        kick(p, dx, dy, speed, Kick.PASS, (1f - skillStat / 115f) * 0.16f);
    }

    private void clearBall(MatchPlayer p, Vector2 aim) {
        Vector2 d = new Vector2(aim);
        if (d.x * dirOf(p.team) < 0.3f) d.x = dirOf(p.team) * 0.8f;
        kick(p, d.x, d.y, 300f + (p.gk ? p.distribution * 0.7f : 40f), Kick.CLEAR, 0.10f);
    }

    private boolean tryTackle(MatchPlayer t) {
        if (t.tackleCd > 0f || t.stun > 0f || t.tackleTimer > 0f || t.gk) return false;
        t.tackleTimer = 0.34f;
        t.tackleCd = 0.95f;
        t.actionTime = 0f;
        t.vel.mulAdd(t.face, 130f);
        MatchPlayer c = carrier;
        if (c != null && c.team != t.team && !c.gk && c.hold <= 0f) {
            float d = t.pos.dst(c.pos);
            if (d <= 21f) {
                float chance = 0.50f + (t.def - c.drib) / 150f;
                chance *= (t.team == 1 ? 0.85f + 0.15f * diff.skill : 1f);
                chance = clamp(chance, 0.22f, 0.82f);
                if (rnd.nextFloat() < chance) {
                    carrier = null;
                    Vector2 away = new Vector2(c.pos).sub(t.pos);
                    if (away.len2() < 1f) away.set(t.face);
                    away.nor().rotateDeg((rnd.nextFloat() - 0.5f) * 70f);
                    ballVel.set(away).scl(110f + rnd.nextFloat() * 50f);
                    c.stun = 0.45f; c.fallen = true; c.noPickup = 0.45f;
                    lastTouch = t; lastTouchTeam = t.team;
                    ballIsShot = false;
                    stats[t.team].tackles++;
                    emit(Ev.TACKLE, t, c.pos.x, c.pos.y, "");
                } else {
                    t.stun = 0.5f;
                    emit(Ev.TACKLE_FAIL, t, t.pos.x, t.pos.y, "");
                }
                return true;
            }
        } else if (c == null && t.pos.dst(ballPos) < 17f && ballVel.len() < 220f) {
            takeBall(t);
        }
        emit(Ev.TACKLE_FAIL, t, t.pos.x, t.pos.y, "");
        return true;
    }

    private void takeBall(MatchPlayer p) {
        carrier = p;
        ballIsShot = false;
        ballVel.setZero();
        lastTouch = p;
        lastTouchTeam = p.team;
        p.noPickup = 0f;
        if (p.team == 0 && !p.gk) controlled = p;
        if (p.team == 0 && p.gk) controlled = p;
    }

    // =====================================================================
    //  Bola, pengambilan, kiper
    // =====================================================================

    private void updateBall(float dt) {
        if (carrier != null) {
            Vector2 target = new Vector2(carrier.pos).mulAdd(carrier.face, carrier.gk ? 5f : 7f);
            ballPos.lerp(target, Math.min(1f, 20f * dt));
            ballVel.setZero();
            return;
        }
        ballPos.mulAdd(ballVel, dt);
        float sp = ballVel.len();
        if (sp > 0f) {
            sp = Math.max(0f, sp * Math.max(0f, 1f - 1.15f * dt) - 12f * dt);
            if (sp < 6f) { ballVel.setZero(); sp = 0f; } else ballVel.nor().scl(sp);
        }
        if (ballIsShot && sp < 110f) ballIsShot = false;
    }

    private void pickups() {
        if (carrier != null) return;
        float speed = ballVel.len();
        MatchPlayer best = null; float bd = 1e9f;
        for (MatchPlayer p : all) {
            if (p.noPickup > 0f || p.stun > 0f || p.celebrate > 0f) continue;
            if (p.gk && ballIsShot && p.team != shotTeam) continue;   // tembakan lawan: diputuskan oleh goalkeeperSaves()
            float reach = p.gk ? 17f : (p.tackleTimer > 0f ? 15f : 11f);
            float d = p.pos.dst(ballPos);
            if (d > reach) continue;
            float maxTake = p.gk ? 430f : 185f + p.drib * 0.8f;
            if (speed > maxTake) {
                // tembakan keras mengenai badan pemain bertahan -> terblok
                if (ballIsShot && p.team != shotTeam && !p.gk && d < 10f) {
                    if (rnd.nextFloat() < 0.5f + p.def / 250f) {
                        ballVel.scl(-0.35f).rotateDeg((rnd.nextFloat() - 0.5f) * 100f);
                        ballIsShot = false;
                        lastTouch = p; lastTouchTeam = p.team;
                        p.noPickup = 0.3f;
                        emit(Ev.BLOCK, p, ballPos.x, ballPos.y, "");
                    }
                }
                continue;
            }
            if (d < bd) { bd = d; best = p; }
        }
        if (best != null) {
            takeBall(best);
            if (best.gk) best.hold = 1.3f;
        }
    }

    private void goalkeeperSaves() {
        if (carrier != null || !ballIsShot) return;
        MatchPlayer g = squad[1 - shotTeam][0];
        if (g.noSave > 0f || g.stun > 0f) return;
        float reach = 19f + g.diving * 0.06f;
        float d = g.pos.dst(ballPos);
        if (d > reach) return;
        float speed = ballVel.len();
        float base = 0.58f + (g.reflex * 0.6f + g.handling * 0.4f - 60f) / 100f - clamp((speed - 230f) / 600f, 0f, 0.30f);
        float chance = clamp(base * (g.team == 1 ? diff.skill : 1f), 0.08f, 0.92f);
        g.noSave = 0.5f;
        if (rnd.nextFloat() < chance) {
            stats[g.team].saves++;
            float catchP = 0.35f + g.handling / 200f - (speed - 200f) / 700f;
            emit(Ev.SAVE, g, ballPos.x, ballPos.y, "Tangkapan!");
            if (rnd.nextFloat() < catchP) {
                takeBall(g);
                g.hold = 1.3f;
                g.actionTime = 0f;
            } else {
                Vector2 v = new Vector2(ballVel).nor();
                float out = dirOf(g.team);
                ballVel.set(out * (70f + rnd.nextFloat() * 80f), (rnd.nextFloat() - 0.5f) * 220f);
                ballVel.scl(0.9f);
                ballIsShot = false;
                lastTouch = g; lastTouchTeam = g.team;
                g.noPickup = 0.35f;
                g.actionTime = 0f;
                v.setZero();
            }
        }
    }

    // =====================================================================
    //  Batas lapangan, gol, restart
    // =====================================================================

    private void checkBoundsAndGoal() {
        float x = ballPos.x, y = ballPos.y;
        if (x < GL || x > GR) {
            if (Math.abs(y - CY) < GOAL_HALF - 2f) {
                // yang menyerang gawang kanan (x > GR) = tim 0
                scoreGoal(x > GR ? 0 : 1);
                return;
            }
        }
        if (carrier != null) return;
        if (y < TB || y > TT) {
            int team = 1 - Math.max(lastTouchTeam, 0);
            float sx = clamp(x, GL + 4f, GR - 4f);
            float sy = y < TB ? TB : TT;
            restart(team, sx, sy, "Lemparan ke dalam", false, y < TB ? -6f : 6f, 0f);
            return;
        }
        if (x < GL || x > GR) {
            boolean left = x < GL;
            int defending = left ? 0 : 1;
            if (lastTouchTeam == defending) {
                int att = 1 - defending;
                float cx = left ? GL : GR;
                float cy = y > CY ? TT : TB;
                stats[att].corners++;
                restart(att, cx, cy, "Tendangan sudut", false, left ? -6f : 6f, y > CY ? 6f : -6f);
            } else {
                float gx = left ? GL + 24f : GR - 24f;
                restart(defending, gx, y > CY ? CY + 24f : CY - 24f, "Tendangan gawang", true, 0f, 0f);
            }
        }
    }

    private void restart(int team, float x, float y, String text, boolean useGk, float offX, float offY) {
        MatchPlayer taker = useGk ? squad[team][0] : nearestOutfield(team, x, y);
        if (taker == null) taker = squad[team][0];
        taker.pos.set(x + offX, y + offY);
        taker.vel.setZero();
        taker.hold = 1.0f;
        taker.stun = 0f;
        Vector2 toCenter = new Vector2(CX - x, CY - y).nor();
        taker.face.set(toCenter);
        carrier = taker;
        ballVel.setZero();
        ballPos.set(x, y);
        ballIsShot = false;
        lastTouch = taker;
        lastTouchTeam = team;
        if (team == 0) controlled = taker;
        // jauhkan lawan dari titik restart
        for (MatchPlayer o : squad[1 - team]) {
            float d = o.pos.dst(x, y);
            if (d < 42f && !o.gk) {
                Vector2 push = new Vector2(o.pos).sub(x, y);
                if (push.len2() < 1f) push.set(toCenter);
                o.pos.set(x, y).mulAdd(push.nor(), 44f);
                o.pos.x = clamp(o.pos.x, GL + 2f, GR - 2f);
                o.pos.y = clamp(o.pos.y, TB, TT);
            }
        }
        emit(Ev.OUT, taker, x, y, text);
    }

    private void scoreGoal(int team) {
        score[team]++;
        scoringTeam = team;
        MatchPlayer scorer = (lastTouch != null && lastTouch.team == team) ? lastTouch : null;
        boolean og = lastTouch != null && lastTouch.team != team;
        String nm = scorer != null ? scorer.name : (lastTouch != null ? lastTouch.name : "?");
        if (scorer != null) scorer.goals++;
        result.goals.add(new MatchResult.Goal(team, nm, minute(), og));
        carrier = null;
        phase = Phase.GOAL;
        phaseTimer = 3.4f;
        netAnim = 0.8f;
        // perayaan: pencetak gol + 3 rekan terdekat
        if (scorer != null) {
            scorer.celebrate = 3f;
            scorer.actionTime = 0f;
            int n = 0;
            List<MatchPlayer> mates = new ArrayList<>();
            for (MatchPlayer p : squad[team]) if (p != scorer && !p.gk) mates.add(p);
            final MatchPlayer sc = scorer;
            mates.sort((a, b) -> Float.compare(a.pos.dst2(sc.pos), b.pos.dst2(sc.pos)));
            for (MatchPlayer p : mates) { if (n++ >= 3) break; p.celebrate = 2.6f; p.actionTime = 0f; }
        }
        emit(Ev.GOAL, scorer, ballPos.x, ballPos.y, og ? nm + " (bunuh diri)" : nm);
    }

    // =====================================================================
    //  AI
    // =====================================================================

    private void computeChasers() {
        for (int t = 0; t < 2; t++) {
            chaser[t][0] = chaser[t][1] = null;
            float rx = carrier != null ? carrier.pos.x : ballPos.x + ballVel.x * 0.25f;
            float ry = carrier != null ? carrier.pos.y : ballPos.y + ballVel.y * 0.25f;
            float b0 = 1e9f, b1 = 1e9f;
            for (MatchPlayer p : squad[t]) {
                if (p.gk || p == controlled || p.stun > 0f) continue;
                float d = p.pos.dst2(rx, ry);
                if (d < b0) { b1 = b0; chaser[t][1] = chaser[t][0]; b0 = d; chaser[t][0] = p; }
                else if (d < b1) { b1 = d; chaser[t][1] = p; }
            }
        }
    }

    private void aiThink(float dt) {
        for (MatchPlayer p : all) {
            if (p == controlled) continue;
            p.thinkTimer -= dt;
            if (p.thinkTimer <= 0f) {
                p.thinkTimer = (0.10f + rnd.nextFloat() * 0.10f) / (p.team == 1 ? diff.skill : 1f);
                think(p);
            }
            steer(p);
        }
    }

    private void steer(MatchPlayer p) {
        if (p.stun > 0f || p.celebrate > 0f || (carrier == p && p.hold > 0f)) { p.desired.setZero(); return; }
        float dx = p.tx - p.pos.x, dy = p.ty - p.pos.y;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d < 2.5f) { p.desired.setZero(); return; }
        float k = Math.min(1f, d / 16f) * p.targetSpeed;
        p.desired.set(dx / d * k, dy / d * k);
    }

    private void think(MatchPlayer p) {
        if (p.stun > 0f) return;
        if (p.gk) gkThink(p);
        else if (carrier == p) carrierThink(p);
        else offBallThink(p);
    }

    private float nearestOppDist(float x, float y, int team) {
        float best = 1e9f;
        for (MatchPlayer o : squad[1 - team]) best = Math.min(best, o.pos.dst(x, y));
        return best;
    }

    private boolean laneBlocked(float x0, float y0, float x1, float y1, int team) {
        float vx = x1 - x0, vy = y1 - y0;
        float len2 = vx * vx + vy * vy;
        if (len2 < 1f) return false;
        for (MatchPlayer o : squad[1 - team]) {
            float t = ((o.pos.x - x0) * vx + (o.pos.y - y0) * vy) / len2;
            if (t < 0.12f || t > 0.95f) continue;
            float px = x0 + vx * t, py = y0 + vy * t;
            if (Vector2.dst(px, py, o.pos.x, o.pos.y) < (o.gk ? 9f : 13f)) return true;
        }
        return false;
    }

    private void carrierThink(MatchPlayer p) {
        int t = p.team;
        float dx = dirOf(t);
        float gx = goalX(t);
        float sk = skillOf(t);
        if (p.hold > 0f) return;
        float distGoal = Vector2.dst(p.pos.x, p.pos.y, gx, CY);
        float press = nearestOppDist(p.pos.x, p.pos.y, t);

        // 1) tembak
        if (distGoal < 185f && Math.abs(p.pos.y - CY) < 125f) {
            boolean clear = !laneBlocked(p.pos.x, p.pos.y, gx, CY, t);
            float want = (1f - distGoal / 200f) * 0.9f * (clear ? 1f : 0.35f) + p.shoot / 450f;
            if (distGoal < 95f && clear) want += 0.25f;
            if (rnd.nextFloat() < want * 0.5f * sk) { shootAI(p); return; }
        }
        // 2) umpan
        boolean pressed = press < 28f;
        boolean deep = (p.pos.x - ownGoalX(t)) * dx < 150f;
        float passWant = pressed ? 0.75f : (deep ? 0.35f : 0.10f);
        if (rnd.nextFloat() < passWant) {
            MatchPlayer tg = bestPassTarget(p);
            if (tg != null) { passTo(p, tg, false); return; }
            if (pressed && deep) { clearBall(p, new Vector2(dx, (rnd.nextFloat() - 0.5f))); return; }
        }
        // 3) giring ke gawang lawan, hindari lawan di depan
        Vector2 dir = new Vector2(dx, clamp((CY - p.pos.y) * 0.010f, -0.6f, 0.6f));
        for (MatchPlayer o : squad[1 - t]) {
            float fx = (o.pos.x - p.pos.x) * dx, fy = o.pos.y - p.pos.y;
            if (fx > 0f && fx < 55f && Math.abs(fy) < 24f) dir.y += (fy > 0 ? -1f : 1f) * 0.9f * (1f - fx / 55f);
        }
        dir.nor();
        p.tx = clamp(p.pos.x + dir.x * 55f, GL + 6f, GR - 6f);
        p.ty = clamp(p.pos.y + dir.y * 55f, TB + 6f, TT - 6f);
        p.targetSpeed = 1f;
        p.wantSprint = press > 45f && p.stamina > 45f;
    }

    private void shootAI(MatchPlayer p) {
        int t = p.team;
        MatchPlayer gk = squad[1 - t][0];
        float side = gk.pos.y > CY ? -1f : 1f;
        if (rnd.nextFloat() < 0.25f) side = 0f;
        float aimY = CY + side * (GOAL_HALF - 7f) + (rnd.nextFloat() - 0.5f) * 8f;
        float power = 0.65f + rnd.nextFloat() * 0.35f;
        kick(p, goalX(t) - p.pos.x, aimY - p.pos.y, shotSpeed(p, power), Kick.SHOT,
                (1f - p.shoot / 125f) * 0.12f * (0.6f + 0.8f * power));
    }

    private MatchPlayer bestPassTarget(MatchPlayer p) {
        int t = p.team; float dx = dirOf(t);
        MatchPlayer best = null; float bs = -1e9f;
        boolean deep = (p.pos.x - ownGoalX(t)) * dx < 150f;
        for (MatchPlayer q : squad[t]) {
            if (q == p) continue;
            float ex = q.pos.x - p.pos.x, ey = q.pos.y - p.pos.y;
            float d = (float) Math.sqrt(ex * ex + ey * ey);
            if (d < 26f || d > 270f) continue;
            float progress = ex * dx / 100f;
            float open = Math.min(nearestOppDist(q.pos.x, q.pos.y, t), 60f) / 40f;
            float s = progress * 0.9f + open - d / 230f;
            if (laneBlocked(p.pos.x, p.pos.y, q.pos.x, q.pos.y, t)) s -= 1.3f;
            if (q.gk) s -= (deep ? 0.3f : 2f);
            if (s > bs) { bs = s; best = q; }
        }
        return bs > -0.4f ? best : null;
    }

    private void gkDistribute(MatchPlayer g) {
        MatchPlayer tg = bestPassTarget(g);
        if (tg != null && rnd.nextFloat() < 0.65f) passTo(g, tg, false);
        else clearBall(g, new Vector2(dirOf(g.team), (rnd.nextFloat() - 0.5f) * 0.8f));
    }

    private void gkThink(MatchPlayer g) {
        int t = g.team;
        float in = dirOf(t);
        float gx = ownGoalX(t);
        float fx = gx + in * 13f;
        g.wantSprint = false; g.targetSpeed = 1f;
        if (carrier == g) {
            if (g.hold <= 0f) gkDistribute(g);
            g.tx = g.pos.x; g.ty = g.pos.y; g.targetSpeed = 0f;
            return;
        }
        boolean incoming = carrier == null && ballIsShot && shotTeam != t && ballVel.x * in < -20f;
        if (incoming) {
            if (g.shotSeenAt < 0f) g.shotSeenAt = Math.max(0.02f, (0.24f - g.reflex * 0.0020f) / skillOf(t));
            if (g.shotSeenAt <= 0f) {
                float tt = (fx - ballPos.x) / ballVel.x;
                if (tt > 0f) {
                    float iy = ballPos.y + ballVel.y * tt;
                    iy += (1f - g.positioning / 100f) * 14f * (rnd.nextFloat() * 2f - 1f);
                    iy = clamp(iy, CY - GOAL_HALF - 8f, CY + GOAL_HALF + 8f);
                    g.tx = fx; g.ty = iy;
                    if (Math.abs(iy - g.pos.y) > 10f && g.diveTimer <= 0f) {
                        g.diveTimer = 0.55f; g.diveDir = iy > g.pos.y ? 1 : -1; g.actionTime = 0f;
                    }
                    return;
                }
            }
            return; // masih bereaksi: tetap di tempat
        } else {
            g.shotSeenAt = -1f;
        }
        // bola lepas dekat gawang -> keluar menjemput bila lebih dekat dari penyerang
        if (carrier == null && !ballIsShot) {
            float dGoal = Vector2.dst(ballPos.x, ballPos.y, gx, CY);
            float dMe = g.pos.dst(ballPos);
            float dOpp = nearestOppDist(ballPos.x, ballPos.y, t);
            if (dGoal < 95f && dMe < dOpp + 6f && Math.abs(ballPos.y - CY) < 70f) {
                g.tx = ballPos.x; g.ty = ballPos.y; return;
            }
        }
        float bx = ballPos.x, by = ballPos.y;
        float distBall = Vector2.dst(bx, by, gx, CY);
        float step = clamp(distBall * 0.10f, 0f, 24f);
        g.tx = fx + in * step * 0.6f;
        g.ty = clamp(CY + (by - CY) * 0.42f, CY - GOAL_HALF + 3f, CY + GOAL_HALF - 3f);
    }

    private boolean isChaser(MatchPlayer p) { return chaser[p.team][0] == p || chaser[p.team][1] == p; }

    private void offBallThink(MatchPlayer p) {
        int t = p.team;
        float dx = dirOf(t);
        boolean has = carrier != null && carrier.team == t;
        boolean opp = carrier != null && carrier.team != t;
        float sk = skillOf(t);
        p.wantSprint = false; p.targetSpeed = 1f;

        if (carrier == null) {
            boolean mid = ballPos.x > GL + 150f && ballPos.x < GR - 150f;
            if (chaser[t][0] == p || (mid && chaser[t][1] == p && t == (lastTouchTeam == 0 ? 1 : 0))) {
                float d = p.pos.dst(ballPos);
                float lead = Math.min(0.45f, d / 220f);
                p.tx = ballPos.x + ballVel.x * lead; p.ty = ballPos.y + ballVel.y * lead;
                p.wantSprint = d > 45f && p.stamina > 25f;
                if (d < 16f && p.tackleCd <= 0f && !ballIsShot && ballVel.len() < 200f && rnd.nextFloat() < 0.3f) tryTackle(p);
                return;
            }
        } else if (opp) {
            if (chaser[t][0] == p) {
                float d = p.pos.dst(carrier.pos);
                p.tx = carrier.pos.x + carrier.vel.x * 0.25f; p.ty = carrier.pos.y + carrier.vel.y * 0.25f;
                p.wantSprint = d > 38f && p.stamina > 25f;
                if (d < 19f && p.tackleCd <= 0f && carrier.hold <= 0f && rnd.nextFloat() < 0.5f * sk) tryTackle(p);
                return;
            }
            if (chaser[t][1] == p && !isForward(p)) {
                // tutup jalur: di antara bola dan gawang sendiri
                float ox = ownGoalX(t), oy = CY;
                Vector2 v = new Vector2(ox - carrier.pos.x, oy - carrier.pos.y).nor();
                p.tx = carrier.pos.x + v.x * 34f; p.ty = carrier.pos.y + v.y * 34f;
                return;
            }
        }

        // ---- posisi bentuk (formasi dinamis) ----
        float W = GR - GL, H = TT - TB;
        float xn0 = t == 0 ? p.homeX : 1f - p.homeX;            // 0 = gawang sendiri
        float a = (ballPos.x - GL) / W; if (t == 1) a = 1f - a;  // kemajuan bola 0..1
        float k = roleShift(p.role);
        float push = has ? (isForward(p) ? 0.08f : (p.role.contains("C") ? 0.05f : 0.02f)) : (opp ? -0.06f : -0.02f);
        float xn = xn0 + (a - 0.5f) * k + push;
        if (has && isForward(p) && xn < a + 0.08f) xn = Math.min(0.93f, a + 0.10f);   // lari ke depan bola
        xn = clamp(xn, 0.08f, 0.94f);
        float yn = (t == 0 ? p.homeY : p.homeY) + ((ballPos.y - TB) / H - 0.5f) * 0.30f;
        yn = clamp(yn, 0.06f, 0.94f);
        float sx = t == 0 ? GL + xn * W : GR - xn * W;
        float sy = TB + yn * H;

        // bertahan: jaga lawan terdekat di area sendiri
        if (opp && isDefender(p)) {
            MatchPlayer mark = null; float bd = 80f;
            for (MatchPlayer o : squad[1 - t]) {
                if (o.gk || o == carrier) continue;
                float d = o.pos.dst(sx, sy);
                float inOwnHalf = (o.pos.x - ownGoalX(t)) * dx;
                if (d < bd && inOwnHalf < 0.62f * W) { bd = d; mark = o; }
            }
            if (mark != null) {
                sx = mark.pos.x - dx * 14f; sy = mark.pos.y;
            }
        }
        p.tx = clamp(sx, GL + 8f, GR - 8f);
        p.ty = clamp(sy, TB + 8f, TT - 8f);
        float d = p.pos.dst(p.tx, p.ty);
        p.wantSprint = d > 80f && p.stamina > 40f;
        p.targetSpeed = d > 40f ? 1f : 0.7f;
    }

    private static boolean isForward(MatchPlayer p) {
        return p.role.equals("ST") || p.role.equals("RW") || p.role.equals("LW") || p.role.equals("CAM");
    }
    private static boolean isDefender(MatchPlayer p) {
        return p.role.equals("CB") || p.role.equals("RB") || p.role.equals("LB") || p.role.equals("CDM");
    }
    private static float roleShift(String r) {
        switch (r) {
            case "CB": return 0.18f; case "RB": case "LB": return 0.30f; case "CDM": return 0.40f;
            case "CM": return 0.50f; case "CAM": return 0.55f; case "RW": case "LW": return 0.55f;
            default: return 0.50f;
        }
    }

    // =====================================================================
    //  Gerak pemain
    // =====================================================================

    private void movePlayers(float dt) {
        boolean frozen = phase == Phase.KICKOFF || phase == Phase.HALFTIME;
        for (MatchPlayer p : all) {
            if (frozen) p.desired.setZero();
            boolean incomingGk = p.gk && p.shotSeenAt <= 0f && p.shotSeenAt > -0.5f && ballIsShot && shotTeam != p.team;
            float vmax = p.maxSpeed() * speedOf(p.team);
            if (carrier == p) vmax *= 0.93f;
            p.sprinting = p.wantSprint && p.stamina > 4f && !p.gk && p.desired.len2() > 0.04f;
            if (p.sprinting) vmax *= 1.28f;
            vmax *= 0.82f + 0.18f * (p.stamina / 100f);
            if (p.gk && p.diveTimer > 0f) vmax = 150f + p.diving * 0.9f;
            if (p.tackleTimer > 0f) vmax *= 0.35f;
            if (p.kickTimer > 0f && !p.gk) vmax *= 0.6f;
            if (p.charging) vmax *= 0.8f;
            if (p.stun > 0f || p.celebrate > 0f) vmax = 0f;
            Vector2 want = new Vector2(p.desired).scl(vmax);
            if (p.celebrate > 0f && phase == Phase.GOAL) want.setZero();
            Vector2 dv = new Vector2(want).sub(p.vel);
            float len = dv.len(), accel = p.gk ? 1100f : 620f;
            float maxd = accel * dt;
            if (len > maxd) dv.scl(maxd / len);
            p.vel.add(dv);
            p.pos.mulAdd(p.vel, dt);

            // stamina
            float sp = p.vel.len();
            if (p.sprinting) p.stamina = Math.max(0f, p.stamina - 20f * dt);
            else p.stamina = Math.min(100f, p.stamina + (sp < 20f ? 14f : 6f) * dt);

            // arah hadap
            if (sp > 14f) {
                p.face.lerp(new Vector2(p.vel).nor(), Math.min(1f, 14f * dt));
                if (p.face.len2() < 1e-4f) p.face.set(p.vel).nor();
                p.face.nor();
            } else if (p != controlled && carrier != p && p.celebrate <= 0f && phase != Phase.GOAL) {
                Vector2 tb = new Vector2(ballPos).sub(p.pos);
                if (tb.len2() > 1f) { p.face.lerp(tb.nor(), Math.min(1f, 6f * dt)); p.face.nor(); }
            }

            // batas
            if (p.hold <= 0f || p.gk) {
                float minX = GL + 3f, maxX = GR - 3f;
                p.pos.y = clamp(p.pos.y, TB, TT);
                p.pos.x = clamp(p.pos.x, minX, maxX);
            } else {
                p.pos.x = clamp(p.pos.x, GL - 8f, GR + 8f);
                p.pos.y = clamp(p.pos.y, TB - 10f, TT + 10f);
            }
            if (incomingGk) { /* kiper menyelam: kecepatan sudah diatur lewat diveTimer */ }
        }
        // pemisahan antar pemain
        for (int i = 0; i < all.size(); i++) {
            MatchPlayer a = all.get(i);
            for (int j = i + 1; j < all.size(); j++) {
                MatchPlayer b = all.get(j);
                float dx = b.pos.x - a.pos.x, dy = b.pos.y - a.pos.y;
                float d2 = dx * dx + dy * dy;
                if (d2 < 81f && d2 > 1e-4f) {
                    float d = (float) Math.sqrt(d2), push = (9f - d) * 0.5f;
                    float nx = dx / d, ny = dy / d;
                    a.pos.add(-nx * push, -ny * push);
                    b.pos.add(nx * push, ny * push);
                }
            }
        }
    }
}
