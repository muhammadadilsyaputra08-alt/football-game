package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.engine.Formations;
import com.cozypixel.soccer.engine.MatchConfig;
import com.cozypixel.soccer.engine.MatchEngine;
import com.cozypixel.soccer.engine.MatchResult;
import com.cozypixel.soccer.engine.OpponentFactory;
import com.cozypixel.soccer.model.TournamentRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Turnamen knockout sungguhan: setiap babak dimainkan di lapangan (tidak ada seri: perpanjangan
 * golden goal + adu penalti), laga lain disimulasikan. Hadiah diberikan di akhir.
 */
public class TournamentScreen extends BaseScreen {
    private static final String[] CUPS = {"Beginner Cup", "Cozy League Cup", "Grand Pixel Cup"};
    private static final int[] TEAM_COUNTS = {4, 8, 8};
    private static final int[] REQ_OVR = {55, 65, 75};
    private static final int[] REWARD = {500, 1200, 2500};
    private static final int[] RUNNER_UP = {200, 500, 1000};
    private static final int[] SEMI = {100, 250, 400};
    private static final MatchEngine.Difficulty[] DIFF = {MatchEngine.Difficulty.EASY, MatchEngine.Difficulty.NORMAL, MatchEngine.Difficulty.HARD};
    private final Random rnd = new Random();

    public TournamentScreen(PixelSoccerGame game) {
        super(game, new Color(0.18f, 0.28f, 0.38f, 1f));
        frame("TOURNAMENT");
        if (game.activeRun != null) showRun(); else showCups();
    }

    private float teamOvr() { return MatchLauncher.ovr(game); }

    // ---------------- daftar piala ----------------
    private void showCups() {
        infoLabel.setText(String.format("Koin: %d   |   OVR tim: %.0f   |   Trofi: %d",
                game.saveData.club.coins, teamOvr(), game.saveData.progress.trophies));
        content.clear();
        for (int i = 0; i < CUPS.length; i++) {
            final int idx = i;
            final String cup = CUPS[i];
            boolean unlocked = Boolean.TRUE.equals(game.saveData.tournamentsUnlocked.get(cup));
            String best = game.saveData.bestResults.containsKey(cup) ? game.saveData.bestResults.get(cup) : "-";
            String text = cup + " (" + TEAM_COUNTS[i] + " tim)\nSaran OVR " + REQ_OVR[i]
                    + "  |  Juara +" + REWARD[i] + "\nBest: " + best + (unlocked ? "" : "   [LOCKED]");
            TextButton btn = button(text, new Runnable() {
                @Override public void run() { startRun(idx); }
            });
            btn.setDisabled(!unlocked);
            content.add(btn).width(340).height(72).row();
        }
        say("Pilih piala. Kalah = tersingkir. Hasil laga disimpan setelah turnamen selesai.");
    }

    private void startRun(int idx) {
        List<String> pool = new ArrayList<>();
        Collections.addAll(pool, OpponentFactory.CLUB_NAMES);
        Collections.shuffle(pool, rnd);
        int n = TEAM_COUNTS[idx];
        String[] names = new String[n];
        float[] str = new float[n];
        int[] emb = new int[n];
        names[0] = game.saveData.club.name;
        emb[0] = 1;
        for (int i = 1; i < n; i++) {
            names[i] = pool.get(i - 1);
            str[i] = REQ_OVR[idx] + (rnd.nextFloat() - 0.5f) * 12f;
            emb[i] = 2 + (i % 5);
        }
        game.activeRun = new TournamentRun(idx, CUPS[idx], names, str, emb);
        go(new TournamentScreen(game));
    }

    // ---------------- bagan ----------------
    private void showRun() {
        final TournamentRun run = game.activeRun;
        infoLabel.setText(run.cupName + "   |   OVR tim: " + Math.round(teamOvr()));
        content.clear();
        if (run.eliminated || run.champion) {
            content.add(new Label(run.champion ? "JUARA " + run.cupName + "!" : "Tersingkir: " + run.finishedLabel, skin, "title")).row();
            for (String l : run.log) content.add(new Label(l, skin, "dim")).row();
            content.add(button("Selesai & Ambil Hadiah", new Runnable() {
                @Override public void run() { conclude(run); }
            })).width(280).height(40).padTop(10).row();
            return;
        }
        content.add(new Label(run.roundName(), skin, "title")).row();
        StringBuilder sb = new StringBuilder("Masih bertahan: ");
        for (int i = 0; i < run.alive.size(); i++) sb.append(i > 0 ? ", " : "").append(run.teamNames[run.alive.get(i)]);
        Label alive = new Label(sb.toString(), skin, "dim");
        alive.setWrap(true);
        content.add(alive).width(520).row();
        for (String l : run.log) content.add(new Label(l, skin, "dim")).row();
        final int opp = run.opponent();
        content.add(new Label("Lawan: " + run.teamNames[opp] + "  (OVR ~" + Math.round(run.strength[opp] + run.round * 3) + ")", skin)).padTop(8).row();
        content.add(button("MAIN " + run.roundName().toUpperCase(), new Runnable() {
            @Override public void run() { play(run, opp); }
        })).width(300).height(44).padTop(6).row();
        content.add(button("Menyerah (keluar turnamen)", new Runnable() {
            @Override public void run() { game.activeRun = null; go(new TournamentScreen(game)); }
        })).width(240).height(30).padTop(6).row();
    }

    private void play(final TournamentRun run, int opp) {
        MatchConfig cfg = new MatchConfig();
        cfg.title = run.cupName + " - " + run.roundName();
        cfg.home = MatchLauncher.homeSpec(game);
        float s = run.strength[opp] + run.round * 3f + (run.alive.size() == 2 ? 3f : 0f);
        cfg.away = OpponentFactory.create(run.teamNames[opp], s, Formations.NAMES[rnd.nextInt(Formations.NAMES.length)], run.emblem[opp], rnd.nextLong());
        cfg.matchSeconds = 100f;
        cfg.difficulty = DIFF[run.cupIdx];
        cfg.allowDraw = false;
        cfg.rewards = false;
        go(new MatchScreen(game, cfg, new MatchScreen.Next() {
            @Override public Screen next(PixelSoccerGame g, MatchResult r) {
                if (g.activeRun != null) g.activeRun.advance(!r.forfeit && r.winner() == 0, new Random());
                return new TournamentScreen(g);
            }
        }));
    }

    private void conclude(TournamentRun run) {
        int idx = run.cupIdx;
        String cup = CUPS[idx];
        int coins;
        String msg;
        if (run.champion) {
            coins = REWARD[idx];
            game.saveData.bestResults.put(cup, "Champion");
            game.saveData.progress.trophies++;
            if (!game.saveData.completedTournaments.contains(cup)) game.saveData.completedTournaments.add(cup);
            if (idx < CUPS.length - 1) game.saveData.tournamentsUnlocked.put(CUPS[idx + 1], true);
            msg = "JUARA " + cup + "! +" + coins + " koin";
        } else {
            coins = "Runner-up".equals(run.finishedLabel) ? RUNNER_UP[idx]
                    : "Semifinal".equals(run.finishedLabel) ? SEMI[idx] : REWARD[idx] / 10;
            String prev = game.saveData.bestResults.get(cup);
            if (prev == null || !"Champion".equals(prev)) game.saveData.bestResults.put(cup, run.finishedLabel);
            msg = "Tersingkir (" + run.finishedLabel + "). Hiburan +" + coins + " koin";
        }
        game.saveData.club.coins += coins;
        game.saveGame();
        game.activeRun = null;
        TournamentScreen next = new TournamentScreen(game);
        next.say(msg);
        go(next);
    }
}
