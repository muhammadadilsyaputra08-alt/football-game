package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.engine.MatchConfig;
import com.cozypixel.soccer.engine.MatchEngine;
import com.cozypixel.soccer.engine.MatchPlayer;
import com.cozypixel.soccer.engine.MatchResult;
import com.cozypixel.soccer.util.Art;
import com.cozypixel.soccer.util.SpriteSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Layar pertandingan: merender MatchEngine (lapangan, pemain, bola, VFX) + HUD + kontrol sentuh/keyboard.
 */
public class MatchScreen extends ScreenAdapter {

    public interface Next { Screen next(PixelSoccerGame game, MatchResult result); }

    private static final float STEP = 1f / 60f;
    private static final String HUD = "UI/Gameplay_HUD/";

    private final PixelSoccerGame game;
    private final MatchConfig cfg;
    private final Next next;
    private final MatchEngine eng;
    private final Art art;
    private final SpriteBatch batch;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final FitViewport vp = new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT, cam);
    private final BitmapFont font = new BitmapFont();
    private final BitmapFont big = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Random rnd = new Random();

    // pause UI
    private final Stage pauseStage;
    private final Table pauseTable = new Table();
    private final Label helpLabel;
    private boolean paused, ended, leaving;
    private float acc, endTimer, introTimer = 2.4f, shake, hintTimer = 14f, fxClock;

    // sprite per pemain
    private final SpriteSheet[] sheets = new SpriteSheet[22];
    private final float[] animT = new float[22];
    private final float[] dustCd = new float[22];

    // referee
    private final Vector2 refPos = new Vector2(MatchEngine.CX, MatchEngine.CY - 30f);
    private float refAnim;
    private boolean refMoving, refLeft;

    // efek
    private static final class Fx {
        TextureRegion[] frames; float x, y, t, fps, scale = 1f;
    }
    private final List<Fx> fxs = new ArrayList<>();
    private final float[] px = new float[120], py = new float[120], pvx = new float[120], pvy = new float[120], plife = new float[120];
    private final Color[] pcol = new Color[120];
    private final Color[] confetti = {Color.valueOf("fba74e"), Color.valueOf("fff4b0"), Color.valueOf("ef6544"),
            Color.valueOf("a4a51e"), Color.valueOf("f4893d")};

    // banner
    private String bannerText = "", bannerSub = "";
    private boolean bannerGoal;
    private float bannerT;

    // input sentuh
    private static final class Btn {
        final String id; final float cx, cy, r; final String up, down; boolean pressed; int pointer = -1;
        Btn(String id, float cx, float cy, float r, String up, String down) {
            this.id = id; this.cx = cx; this.cy = cy; this.r = r; this.up = up; this.down = down;
        }
    }
    private final Btn shoot = new Btn("shoot", 582, 72, 32, HUD + "btn_shoot_pressed.png", HUD + "btn_shoot_pressed.png");
    private final Btn pass = new Btn("pass", 520, 44, 16, HUD + "btn_pass.png", HUD + "btn_pass_pressed.png");
    private final Btn through = new Btn("through", 522, 104, 16, HUD + "btn_through.png", HUD + "btn_through_pressed.png");
    private final Btn sprint = new Btn("sprint", 582, 132, 16, HUD + "btn_sprint_pressed.png", HUD + "btn_sprint_pressed.png");
    private final Btn tackle = new Btn("tackle", 466, 44, 16, HUD + "btn_tackle.png", HUD + "btn_tackle_pressed.png");
    private final Btn swit = new Btn("switch", 466, 92, 16, HUD + "btn_switch.png", HUD + "btn_switch_pressed.png");
    private final Btn[] buttons = {shoot, pass, through, sprint, tackle, swit};
    private int joyPointer = -1;
    private final Vector2 joyBase = new Vector2(), joyKnob = new Vector2(), joyMove = new Vector2();
    private boolean kbShoot;

    // urutan gambar
    private static final class Item { float y; int kind; MatchPlayer p; float x; }
    private final Item[] items = new Item[40];
    private int itemCount;
    private static final Comparator<Item> BY_Y = new Comparator<Item>() {
        @Override public int compare(Item a, Item b) { return Float.compare(b.y, a.y); }
    };

    public MatchScreen(PixelSoccerGame game, MatchConfig cfg, Next next) {
        this.game = game;
        this.cfg = cfg;
        this.next = next;
        this.batch = game.batch;
        this.art = game.art;
        this.eng = new MatchEngine(cfg);
        cam.setToOrtho(false, PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT);
        for (int i = 0; i < items.length; i++) items[i] = new Item();
        for (int i = 0; i < pcol.length; i++) pcol[i] = confetti[0];
        font.getData().setScale(0.8f);
        big.getData().setScale(1.7f);
        for (BitmapFont f : new BitmapFont[]{font, big}) {
            for (TextureRegion r : f.getRegions()) r.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        for (MatchPlayer p : eng.all) sheets[idx(p)] = art.sheet(p.spriteKey);
        eng.setListener(new MatchEngine.Listener() {
            @Override public void onEvent(MatchEngine.Ev ev, MatchPlayer p, float x, float y, String text) { onMatchEvent(ev, p, x, y, text); }
        });

        pauseStage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), batch);
        pauseTable.setFillParent(true);
        pauseTable.add(new Label("JEDA", game.skin, "title")).padBottom(10).row();
        pauseTable.add(btn("Lanjutkan", new Runnable() { public void run() { setPaused(false); } })).width(220).height(36).pad(3).row();
        helpLabel = new Label("", game.skin, "dim");
        helpLabel.setVisible(false);
        pauseTable.add(btn("Kontrol", new Runnable() { public void run() { helpLabel.setVisible(!helpLabel.isVisible()); } })).width(220).height(36).pad(3).row();
        pauseTable.add(btn("Keluar Pertandingan", new Runnable() { public void run() { quit(); } })).width(220).height(36).pad(3).row();
        helpLabel.setText("WASD/Panah: gerak   Shift: lari\nJ/Spasi: tembak (tahan = lebih kuat)\nK: umpan   L: umpan terobosan\nQ: rebut bola   E: ganti pemain   Esc: jeda");
        pauseTable.add(helpLabel).padTop(6);
        pauseTable.setVisible(false);
        pauseStage.addActor(pauseTable);
    }

    private TextButton btn(String text, final Runnable r) {
        TextButton b = new TextButton(text, game.skin);
        b.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { r.run(); }
        });
        return b;
    }

    private static int idx(MatchPlayer p) { return p.team * 11 + p.index; }

    // =====================================================================
    //  Lifecycle & input
    // =====================================================================

    @Override
    public void show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputMultiplexer(pauseStage, new MatchInput()));
    }

    @Override
    public void resize(int w, int h) {
        vp.update(w, h, true);
        pauseStage.getViewport().update(w, h, true);
    }

    private void setPaused(boolean p) {
        paused = p;
        pauseTable.setVisible(p);
        if (p) releaseAll();
    }

    private void releaseAll() {
        for (Btn b : buttons) { b.pressed = false; b.pointer = -1; }
        joyPointer = -1; joyMove.setZero(); kbShoot = false;
    }

    private void quit() {
        if (leaving) return;
        leaving = true;
        MatchResult r = eng.result;
        r.forfeit = true;
        r.homeGoals = 0; r.awayGoals = 3;
        game.go(next.next(game, r));
    }

    private final class MatchInput extends InputAdapter {
        private final Vector2 tmp = new Vector2();

        @Override public boolean keyDown(int k) {
            if (k == Input.Keys.ESCAPE || k == Input.Keys.BACK || k == Input.Keys.P) { setPaused(!paused); return true; }
            if (paused) return false;
            switch (k) {
                case Input.Keys.J: case Input.Keys.SPACE:
                    eng.input.shootPressed = true; kbShoot = true; return true;
                case Input.Keys.K: eng.input.passPressed = true; return true;
                case Input.Keys.L: eng.input.throughPressed = true; return true;
                case Input.Keys.Q: case Input.Keys.U: eng.input.tacklePressed = true; return true;
                case Input.Keys.E: case Input.Keys.TAB: eng.input.switchPressed = true; return true;
                default: return false;
            }
        }
        @Override public boolean keyUp(int k) {
            if (k == Input.Keys.J || k == Input.Keys.SPACE) { kbShoot = false; return true; }
            return false;
        }
        @Override public boolean touchDown(int sx, int sy, int pointer, int button) {
            if (paused) return false;
            vp.unproject(tmp.set(sx, sy));
            float x = tmp.x, y = tmp.y;
            if (Vector2.dst(x, y, 614, 364) < 16) { setPaused(true); return true; }
            for (Btn b : buttons) {
                if (b.pointer == -1 && Vector2.dst(x, y, b.cx, b.cy) <= b.r + 10) {
                    b.pressed = true; b.pointer = pointer;
                    switch (b.id) {
                        case "shoot": eng.input.shootPressed = true; break;
                        case "pass": eng.input.passPressed = true; break;
                        case "through": eng.input.throughPressed = true; break;
                        case "tackle": eng.input.tacklePressed = true; break;
                        case "switch": eng.input.switchPressed = true; break;
                        default: break;
                    }
                    return true;
                }
            }
            if (x < 330 && joyPointer == -1) {
                joyPointer = pointer;
                joyBase.set(Math.max(44, Math.min(x, 300)), Math.max(44, Math.min(y, 250)));
                joyKnob.set(joyBase);
                joyMove.setZero();
                return true;
            }
            return false;
        }
        @Override public boolean touchDragged(int sx, int sy, int pointer) {
            if (pointer != joyPointer) return false;
            vp.unproject(tmp.set(sx, sy));
            Vector2 d = new Vector2(tmp).sub(joyBase);
            float len = d.len();
            if (len > 34f) d.scl(34f / len);
            joyKnob.set(joyBase).add(d);
            d.scl(1f / 34f);
            joyMove.set(d.len() < 0.18f ? Vector2.Zero : d);
            return true;
        }
        @Override public boolean touchUp(int sx, int sy, int pointer, int button) {
            if (pointer == joyPointer) { joyPointer = -1; joyMove.setZero(); return true; }
            for (Btn b : buttons) if (b.pointer == pointer) { b.pointer = -1; b.pressed = false; return true; }
            return false;
        }
    }

    private void pollKeyboard() {
        Vector2 m = new Vector2();
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) m.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) m.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) m.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) m.y -= 1;
        if (m.len2() > 1f) m.nor();
        eng.input.move.set(m.len2() > 0f ? m : joyMove);
        eng.input.sprint = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) || sprint.pressed;
        eng.input.shootHeld = kbShoot || shoot.pressed;
    }

    // =====================================================================
    //  Event dari engine -> efek
    // =====================================================================

    private void spawn(String rel, int frames, float x, float y, float fps, float scale) {
        Fx f = new Fx();
        f.frames = art.strip(rel, frames);
        f.x = x; f.y = y; f.fps = fps; f.scale = scale;
        fxs.add(f);
    }

    private void burst(float x, float y, int n, boolean home) {
        int made = 0;
        for (int i = 0; i < px.length && made < n; i++) {
            if (plife[i] > 0f) continue;
            px[i] = x; py[i] = y;
            float a = rnd.nextFloat() * 6.2832f, s = 40f + rnd.nextFloat() * 130f;
            pvx[i] = (float) Math.cos(a) * s; pvy[i] = (float) Math.sin(a) * s + 60f;
            plife[i] = 1.0f + rnd.nextFloat() * 1.2f;
            pcol[i] = home ? confetti[rnd.nextInt(confetti.length)] : Color.valueOf("b8aca3");
            made++;
        }
    }

    private void banner(String text, String sub, boolean goal) {
        bannerText = text; bannerSub = sub; bannerGoal = goal; bannerT = goal ? 3.2f : 1.7f;
    }

    private void vibrate(int ms) {
        if (game.saveData != null && game.saveData.vibrationOn) {
            try { Gdx.input.vibrate(ms); } catch (Throwable ignored) { }
        }
    }

    private void onMatchEvent(MatchEngine.Ev ev, MatchPlayer p, float x, float y, String text) {
        switch (ev) {
            case KICK: case PASS:
                spawn("VFX/Ball_Kick_Dust_6f_16x16.png", 6, x, y, 16f, 1f); break;
            case SHOT:
                spawn("VFX/Ball_Kick_Dust_6f_16x16.png", 6, x, y, 16f, 1.3f);
                spawn("VFX/Ball_Impact_4f_16x16.png", 4, x, y, 14f, 1f); break;
            case SAVE: case BLOCK:
                spawn("VFX/Ball_Impact_4f_16x16.png", 4, x, y, 14f, 1.6f);
                if (ev == MatchEngine.Ev.SAVE) banner("Penyelamatan!", p != null ? p.name : "", false);
                break;
            case TACKLE:
                spawn("VFX/Tackle_Dust_5f_24x24.png", 5, x, y, 14f, 1f); break;
            case TACKLE_FAIL:
                spawn("VFX/Tackle_Dust_5f_24x24.png", 5, x, y, 14f, 0.7f); break;
            case GOAL: {
                boolean home = eng.scoringTeam == 0;
                float gx = home ? MatchEngine.GR - 8f : MatchEngine.GL + 8f;
                spawn("VFX/Goal_Explosion_8f_64x64.png", 8, gx, MatchEngine.CY - 10f, 12f, 1.5f);
                burst(gx, MatchEngine.CY, 70, home);
                banner("GOL!", text + "  " + eng.minute() + "'", true);
                shake = home ? 5f : 3f;
                vibrate(home ? 160 : 60);
                break;
            }
            case OUT: banner(text, "", false); break;
            case HALFTIME: banner("Turun Minum", eng.names[0] + " " + eng.score[0] + " - " + eng.score[1] + " " + eng.names[1], false); break;
            case EXTRA: banner("Perpanjangan Waktu", "Gol emas: gol pertama menang!", false); break;
            case PENALTIES: banner("Adu Penalti", text, false); break;
            case FULLTIME: banner("Peluit Panjang", "", false); break;
            default: break;
        }
    }

    // =====================================================================
    //  Render loop
    // =====================================================================

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        pollKeyboard();
        if (!paused && !leaving) {
            fxClock += delta;
            if (introTimer > 0f) introTimer -= delta;
            else if (!ended) {
                acc += delta;
                int n = 0;
                while (acc >= STEP && n++ < 5) eng.update(STEP);
                if (acc > STEP * 5) acc = 0;
                hintTimer -= delta;
                if (eng.phase == MatchEngine.Phase.FINISHED) { ended = true; endTimer = 2.6f; }
            } else {
                endTimer -= delta;
                if (endTimer <= 0f) finishAndLeave();
            }
            updateFx(delta);
        }

        Gdx.gl.glClearColor(0.1f, 0.13f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        vp.apply();
        drawWorld();
        drawHud();
        if (paused) {
            batch.setProjectionMatrix(cam.combined);
            batch.begin();
            batch.setColor(0f, 0f, 0f, 0.6f);
            batch.draw(art.white(), 0, 0, 640, 384);
            batch.setColor(Color.WHITE);
            batch.end();
            pauseStage.act(delta);
            pauseStage.draw();
        }
    }

    private void finishAndLeave() {
        if (leaving) return;
        leaving = true;
        MatchResult r = eng.result;
        game.go(new MatchResultScreen(game, cfg, r, next, eng));
    }

    private void updateFx(float dt) {
        for (int i = fxs.size() - 1; i >= 0; i--) {
            Fx f = fxs.get(i);
            f.t += dt;
            if (f.t * f.fps >= f.frames.length) fxs.remove(i);
        }
        for (int i = 0; i < px.length; i++) {
            if (plife[i] <= 0f) continue;
            plife[i] -= dt;
            pvy[i] -= 260f * dt;
            px[i] += pvx[i] * dt; py[i] += pvy[i] * dt;
        }
        if (shake > 0f) shake = Math.max(0f, shake - 12f * dt);
        if (bannerT > 0f) bannerT -= dt;
        // debu sprint
        for (MatchPlayer p : eng.all) {
            int i = idx(p);
            dustCd[i] -= dt;
            if (p.sprinting && dustCd[i] <= 0f && p.vel.len2() > 900f) {
                dustCd[i] = 0.16f;
                spawn("VFX/Sprint_Dust_4f_16x16.png", 4, p.pos.x - p.face.x * 8f, p.pos.y - 2f, 14f, 0.8f);
            }
            boolean moving = p.vel.len2() > 144f;
            if (moving) animT[i] += dt; else animT[i] = 0f;
        }
        // wasit mengikuti bola dari jauh
        float tx = Math.max(MatchEngine.GL + 20f, Math.min(MatchEngine.GR - 20f, eng.ballPos.x - 30f));
        float ty = Math.max(MatchEngine.TB + 8f, Math.min(MatchEngine.TT - 8f, eng.ballPos.y - 34f));
        Vector2 d = new Vector2(tx - refPos.x, ty - refPos.y);
        float len = d.len();
        refMoving = len > 6f;
        if (refMoving) {
            d.nor().scl(Math.min(len, 62f * dt));
            refPos.add(d);
            refLeft = d.x < 0f;
            refAnim += dt;
        }
    }

    // =====================================================================
    //  Gambar dunia
    // =====================================================================

    private void drawWorld() {
        cam.position.set(PixelSoccerGame.V_WIDTH / 2f + (shake > 0 ? (rnd.nextFloat() - 0.5f) * shake : 0f),
                PixelSoccerGame.V_HEIGHT / 2f + (shake > 0 ? (rnd.nextFloat() - 0.5f) * shake : 0f), 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(art.stadium(), 0, 0);

        // gawang (di belakang pemain)
        boolean netL = eng.netAnim > 0f && eng.scoringTeam == 1;
        boolean netR = eng.netAnim > 0f && eng.scoringTeam == 0;
        drawGoal(true, netL);
        drawGoal(false, netR);

        // bayangan
        Texture ps = art.tex("Objects/Football/Player_Shadow.png");
        for (MatchPlayer p : eng.all) batch.draw(ps, p.pos.x - 9, p.pos.y - 7);
        batch.draw(art.tex("Objects/Football/Football_Shadow.png"), eng.ballPos.x - 5, eng.ballPos.y - 4);

        // glow pemain aktif
        MatchPlayer c = eng.controlled;
        if (c != null && eng.phase != MatchEngine.Phase.FINISHED) {
            TextureRegion[] g = art.strip("VFX/Player_Selection_Glow_4f_36x18.png", 4);
            batch.draw(g[(int) (fxClock * 8f) % 4], c.pos.x - 18, c.pos.y - 13);
        }

        // urutkan: y besar digambar dulu
        itemCount = 0;
        for (MatchPlayer p : eng.all) add(0, p.pos.y, p.pos.x, p);
        add(1, eng.ballPos.y - 1f, eng.ballPos.x, null);
        add(2, refPos.y, refPos.x, null);
        add(3, MatchEngine.TB, MatchEngine.GL, null); add(3, MatchEngine.TB, MatchEngine.GR, null);
        add(3, MatchEngine.TT, MatchEngine.GL, null); add(3, MatchEngine.TT, MatchEngine.GR, null);
        Arrays.sort(items, 0, itemCount, BY_Y);
        for (int i = 0; i < itemCount; i++) {
            Item it = items[i];
            switch (it.kind) {
                case 0: drawPlayer(it.p); break;
                case 1: drawBall(); break;
                case 2: drawReferee(); break;
                default: batch.draw(art.tex("Objects/Training_Equipment/Corner_Flag.png"), it.x - 5, it.y - 1); break;
            }
        }

        // jejak tembakan / umpan
        float sp = eng.ballVel.len();
        if (eng.carrier == null && sp > 150f) {
            boolean shot = eng.ballIsShot && sp > 200f;
            TextureRegion[] tr = shot ? art.strip("VFX/Shooting_Trail_4f_32x10.png", 4) : art.strip("VFX/Passing_Trail_4f_28x6.png", 4);
            TextureRegion r = tr[(int) (fxClock * 20f) % 4];
            float ang = (float) Math.toDegrees(Math.atan2(eng.ballVel.y, eng.ballVel.x));
            float w = r.getRegionWidth(), h = r.getRegionHeight();
            batch.draw(r, eng.ballPos.x - w, eng.ballPos.y - h / 2f + 2f, w, h / 2f, w, h, 1f, 1f, ang);
        }

        // indikator pemain aktif & charge
        if (c != null && eng.phase != MatchEngine.Phase.FINISHED) {
            Texture ind = art.tex(HUD + "active_player_indicator.png");
            batch.draw(ind, c.pos.x - 6, c.pos.y + 31 + (float) Math.sin(fxClock * 6f) * 1.5f);
            if (c.charging) {
                float k = Math.min(1f, c.charge / 0.75f);
                batch.setColor(0.22f, 0.05f, 0.08f, 1f);
                batch.draw(art.white(), c.pos.x - 13, c.pos.y - 15, 26, 5);
                batch.setColor(1f - 0.2f * k, 0.55f + 0.4f * k, 0.2f, 1f);
                batch.draw(art.white(), c.pos.x - 12, c.pos.y - 14, 24 * k, 3);
                batch.setColor(Color.WHITE);
            }
        }

        // efek sprite
        for (Fx f : fxs) {
            int fi = Math.min(f.frames.length - 1, (int) (f.t * f.fps));
            TextureRegion r = f.frames[fi];
            float w = r.getRegionWidth() * f.scale, h = r.getRegionHeight() * f.scale;
            batch.draw(r, f.x - w / 2f, f.y - h / 2f, w, h);
        }
        // konfeti
        for (int i = 0; i < px.length; i++) {
            if (plife[i] <= 0f) continue;
            batch.setColor(pcol[i]);
            batch.draw(art.white(), px[i], py[i], 3, 3);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void add(int kind, float y, float x, MatchPlayer p) {
        Item it = items[itemCount++];
        it.kind = kind; it.y = y; it.x = x; it.p = p;
    }

    private void drawGoal(boolean left, boolean anim) {
        float y = MatchEngine.CY - 42f;
        if (anim) {
            TextureRegion[] fr = art.strip(left ? "Environment/Goals/Goal_Left_NetAnim_4f.png" : "Environment/Goals/Goal_Right_NetAnim_4f.png", 4);
            TextureRegion r = fr[Math.min(3, (int) ((0.8f - eng.netAnim) * 10f))];
            float x = left ? MatchEngine.GL - 30f : MatchEngine.GR;
            batch.draw(r, x, y - 1f);
        } else {
            Texture t = art.tex(left ? "Environment/Goals/Goal_Left.png" : "Environment/Goals/Goal_Right.png");
            batch.draw(t, left ? MatchEngine.GL - 28f : MatchEngine.GR, y);
        }
    }

    private TextureRegion fallbackFrame(SpriteSheet sh, String name, float t, String dir, boolean gk) {
        TextureRegion r = sh.frame(name, t);
        if (r == null) r = sh.frame(gk ? "GK_idle_" + dir : "idle_" + dir, t);
        if (r == null) r = sh.frame(gk ? "GK_idle_down" : "idle_down", t);
        return r;
    }

    private void drawPlayer(MatchPlayer p) {
        int i = idx(p);
        SpriteSheet sh = sheets[i];
        String d = p.dirName();
        float tIdle = fxClock + i * 0.13f;
        boolean moving = p.vel.len2() > 144f;
        String lr = p.face.x < 0 ? "left" : "right";
        TextureRegion r;
        float rot = 0f;
        boolean win = eng.phase == MatchEngine.Phase.FINISHED;
        if (p.gk) {
            if (p.celebrate > 0f) r = fallbackFrame(sh, "GK_celebrate", p.actionTime, d, true);
            else if (p.diveTimer > 0f) {
                r = fallbackFrame(sh, p.diveDir > 0 ? "GK_dive_left" : "GK_dive_right", p.actionTime, d, true);
                rot = -90f;
            } else if (eng.carrier == p) r = fallbackFrame(sh, "GK_catch_" + lr, p.actionTime, d, true);
            else if (p.kickTimer > 0f) r = fallbackFrame(sh, "GK_kick_" + lr, p.actionTime, d, true);
            else r = fallbackFrame(sh, (moving ? "GK_move_" : "GK_idle_") + d, moving ? animT[i] : tIdle, d, true);
        } else {
            String name;
            float t = moving ? animT[i] : tIdle;
            boolean concede = eng.phase == MatchEngine.Phase.GOAL && p.celebrate <= 0f && p.team != eng.scoringTeam;
            if (win && eng.result.winner() == p.team) name = "victory";
            else if (win && eng.result.winner() == 1 - p.team) name = "defeat";
            else if (p.celebrate > 0f) { name = "celebrate"; t = p.actionTime; }
            else if (concede) name = "defeat";
            else if (p.stun > 0f && p.fallen) { name = "fall"; t = 0.2f; }
            else if (p.tackleTimer > 0f) { name = "tackle_" + d; t = p.actionTime; }
            else if (p.kickTimer > 0f) { name = (p.kickWasShot ? "shoot_" : "pass_") + d; t = p.actionTime; }
            else if (moving) {
                if (p.sprinting) name = "sprint_" + d;
                else if (eng.carrier == p) name = "dribble_" + d;
                else name = p.vel.len2() > 3600f ? "run_" + d : "walk_" + d;
            } else name = "idle_" + d;
            r = fallbackFrame(sh, name, t, d, false);
        }
        if (r == null) return;
        if (p.team == 1 && p.gk) batch.setColor(0.65f, 0.85f, 1f, 1f);
        else if (p.stun > 0f && !p.fallen && ((int) (fxClock * 20f) & 1) == 0) batch.setColor(1f, 1f, 1f, 0.7f);
        float x = p.pos.x, y = p.pos.y;
        if (rot != 0f) batch.draw(r, x - 16, y - 8, 16, 16, 32, 32, 1f, 1f, rot);
        else batch.draw(r, x - 16, y - 4);
        batch.setColor(Color.WHITE);
    }

    private void drawBall() {
        float sp = eng.ballVel.len();
        float bx = eng.ballPos.x, by = eng.ballPos.y;
        if (sp > 25f) {
            TextureRegion[] f = art.strip("Objects/Football/Football_8x8_Spin_4f.png", 4);
            batch.draw(f[(int) (fxClock * Math.min(30f, sp / 8f)) % 4], bx - 5, by - 1);
        } else {
            batch.draw(art.tex("Objects/Football/Football_8x8.png"), bx - 4, by - 1);
        }
    }

    private void drawReferee() {
        TextureRegion[] f = art.strip("Objects/Referee/Referee_4f.png", 4);
        TextureRegion r;
        if (refMoving) {
            r = f[1 + ((int) (refAnim * 8f) & 1)];
        } else r = eng.ballPos.y > refPos.y ? f[3] : f[0];
        float x = refPos.x - 16, y = refPos.y - 4;
        if (refMoving && refLeft) batch.draw(r, x + 32, y, -32, 32);
        else batch.draw(r, x, y);
    }

    // =====================================================================
    //  HUD
    // =====================================================================

    private void text(BitmapFont f, String s, float x, float y, Color c) {
        f.setColor(0.22f, 0.05f, 0.08f, 1f);
        f.draw(batch, s, x + 1, y - 1);
        f.setColor(c);
        f.draw(batch, s, x, y);
    }

    private void textC(BitmapFont f, String s, float cx, float y, Color c) {
        layout.setText(f, s);
        text(f, s, cx - layout.width / 2f, y, c);
    }

    private void rect(float x, float y, float w, float h, Color c) {
        batch.setColor(c);
        batch.draw(art.white(), x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    private static String clip(String s, int n) { return s.length() <= n ? s : s.substring(0, n - 1) + "."; }

    private void drawHud() {
        cam.position.set(PixelSoccerGame.V_WIDTH / 2f, PixelSoccerGame.V_HEIGHT / 2f, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        Color gold = Color.valueOf("ffd98a"), cream = Color.valueOf("fff4b0");

        // papan skor
        Color maroon = Color.valueOf("390c14");
        rect(168, 346, 304, 34, maroon);
        rect(170, 348, 300, 30, Color.valueOf("2c3a40"));
        batch.draw(art.tex("Branding/Team_Emblem_0" + Math.max(1, Math.min(6, cfg.home.emblem)) + ".png"), 174, 349, 24, 24);
        batch.draw(art.tex("Branding/Team_Emblem_0" + Math.max(1, Math.min(6, cfg.away.emblem)) + ".png"), 442, 349, 24, 24);
        text(font, clip(eng.names[0].toUpperCase(), 11), 202, 369, cream);
        layout.setText(font, clip(eng.names[1].toUpperCase(), 11));
        text(font, clip(eng.names[1].toUpperCase(), 11), 438 - layout.width, 369, cream);
        textC(big, eng.score[0] + " - " + eng.score[1], 320, 374, gold);
        String clock = (eng.phase == MatchEngine.Phase.HALFTIME ? "HT" : eng.minute() + "'") + (eng.extra ? " ET" : "");
        rect(296, 330, 48, 14, maroon);
        rect(298, 332, 44, 10, Color.valueOf("2c3a40"));
        textC(font, clock, 320, 341, cream);

        // tombol jeda
        batch.draw(art.tex(HUD + "btn_pause.png"), 602, 352);

        // info pemain aktif + stamina
        MatchPlayer c = eng.controlled;
        if (c != null) {
            text(font, "#" + c.number + " " + c.name, 8, 372, cream);
            Texture bg = art.tex(HUD + "stamina_bar_bg.png"), fill = art.tex(HUD + "stamina_bar_fill.png");
            batch.draw(bg, 8, 348, 64, 16);
            int w = (int) (32 * Math.max(0f, Math.min(1f, c.stamina / 100f)));
            if (w > 0) batch.draw(fill, 8, 348, 64f * w / 32f, 16, 0, 0, w, 8, false, false);
        }

        // joystick
        if (joyPointer != -1) {
            batch.setColor(1, 1, 1, 0.35f);
            batch.draw(art.white(), joyBase.x - 36, joyBase.y - 36, 72, 72);
            batch.setColor(1, 1, 1, 0.9f);
            batch.draw(art.tex(HUD + "joystick_knob.png"), joyKnob.x - 14, joyKnob.y - 14);
            batch.setColor(Color.WHITE);
        } else {
            batch.setColor(1, 1, 1, 0.25f);
            batch.draw(art.tex(HUD + "joystick_knob.png"), 74, 74);
            batch.setColor(Color.WHITE);
        }
        // tombol aksi
        for (Btn b : buttons) {
            Texture t = art.tex(b.pressed ? b.down : b.up);
            float s = b.r * 2f * (b.pressed ? 0.92f : 1f);
            batch.setColor(b.pressed ? 1f : 0.85f, b.pressed ? 1f : 0.85f, b.pressed ? 1f : 0.85f, b.pressed ? 1f : 0.72f);
            batch.draw(t, b.cx - s / 2f, b.cy - s / 2f, s, s);
        }
        batch.setColor(Color.WHITE);

        // banner kejadian
        if (bannerT > 0f && introTimer <= 0f) {
            if (bannerGoal) {
                float pop = Math.min(1f, (3.2f - bannerT) * 6f + 0.4f);
                float w = 96 * 3 * pop, h = 18 * 3 * pop;
                batch.draw(art.tex(HUD + "match_event_notification.png"), 320 - w / 2f, 262 - h / 2f, w, h);
                textC(font, bannerSub, 320, 232, cream);
            } else {
                layout.setText(font, bannerText);
                float w = Math.max(layout.width, 100) + 24;
                rect(320 - w / 2f - 2, 252, w + 4, bannerSub.isEmpty() ? 26 : 40, maroon);
                rect(320 - w / 2f, 254, w, bannerSub.isEmpty() ? 22 : 36, Color.valueOf("2c3a40"));
                textC(font, bannerText, 320, bannerSub.isEmpty() ? 271 : 287, gold);
                if (!bannerSub.isEmpty()) textC(font, bannerSub, 320, 270, cream);
            }
        }
        if (eng.phase == MatchEngine.Phase.KICKOFF && introTimer <= 0f && bannerT <= 0f) {
            textC(big, "SIAP!", 320, 270, gold);
        }
        // petunjuk keyboard
        if (hintTimer > 0f && introTimer <= 0f) {
            float a = Math.min(1f, hintTimer / 3f);
            Color h = new Color(1f, 0.96f, 0.69f, a);
            textC(font, "WASD/Panah gerak  Shift lari  J tembak (tahan=kuat)  K umpan  L terobosan  Q rebut  E ganti  Esc jeda", 320, 24, h);
        }
        // intro
        if (introTimer > 0f) {
            float a = Math.min(1f, introTimer * 2f);
            batch.setColor(0f, 0f, 0f, 0.78f * a);
            batch.draw(art.white(), 0, 0, 640, 384);
            batch.setColor(1, 1, 1, a);
            batch.draw(art.tex("Branding/Team_Emblem_0" + Math.max(1, Math.min(6, cfg.home.emblem)) + ".png"), 150, 160, 64, 64);
            batch.draw(art.tex("Branding/Team_Emblem_0" + Math.max(1, Math.min(6, cfg.away.emblem)) + ".png"), 426, 160, 64, 64);
            batch.setColor(Color.WHITE);
            textC(big, "VS", 320, 205, gold);
            textC(font, clip(cfg.home.name, 16), 182, 148, cream);
            textC(font, clip(cfg.away.name, 16), 458, 148, cream);
            textC(font, cfg.title + "  |  " + cfg.difficulty.label, 320, 300, cream);
        }
        // akhir laga
        if (ended && !leaving) textC(big, "PELUIT PANJANG", 320, 200, gold);
        font.setColor(Color.WHITE);
        big.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        big.dispose();
        pauseStage.dispose();
    }
}
