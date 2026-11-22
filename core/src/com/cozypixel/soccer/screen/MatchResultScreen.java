package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.engine.MatchConfig;
import com.cozypixel.soccer.engine.MatchEngine;
import com.cozypixel.soccer.engine.MatchPlayer;
import com.cozypixel.soccer.engine.MatchResult;
import com.cozypixel.soccer.model.SaveData;

/** Hasil akhir: skor, pencetak gol, statistik, hadiah koin (diterapkan sekali ke save). */
public class MatchResultScreen extends BaseScreen {
    private final MatchConfig cfg;
    private final MatchResult result;
    private final MatchScreen.Next next;

    public MatchResultScreen(PixelSoccerGame game, MatchConfig cfg, MatchResult result, MatchScreen.Next next, MatchEngine eng) {
        super(game, new Color(0.14f, 0.24f, 0.2f, 1f));
        this.cfg = cfg;
        this.result = result;
        this.next = next;
        if (cfg.rewards && !result.forfeit) applyRewards(eng);
        build();
    }

    private void applyRewards(MatchEngine eng) {
        int w = result.winner();
        int coins = w == 0 ? cfg.rewardWin : (w == -1 ? cfg.rewardDraw : cfg.rewardLose);
        int bonus = Math.min(5, result.homeGoals) * 10;
        boolean clean = result.awayGoals == 0;
        if (clean) bonus += 50;
        coins += bonus;
        SaveData sd = game.saveData;
        sd.club.coins += coins;
        sd.progress.matchesPlayed++;
        if (w == 0) sd.progress.matchesWon++;
        sd.progress.goalsScored += result.homeGoals;
        if (clean) sd.progress.cleanSheets++;
        for (MatchPlayer p : eng.squad[0]) {
            p.model.matchesPlayed++;
            p.model.xp += w == 0 ? 30 : 15;
        }
        result.coinsEarned = coins;
        result.rewardNote = "Bonus gol/clean sheet: +" + bonus;
        game.saveGame();
    }

    private void build() {
        root.top().pad(8);
        int w = result.winner();
        String head = w == 0 ? "MENANG!" : (w == 1 ? "KALAH" : "SERI");
        root.add(new Label(head, skin, "title")).padBottom(2).row();
        root.add(new Label(cfg.title, skin, "dim")).row();
        root.add(new Label(result.homeName + "   " + result.homeGoals + " - " + result.awayGoals + "   " + result.awayName,
                skin)).padTop(4).row();
        if (result.penHome >= 0) {
            root.add(new Label("Adu penalti: " + result.penHome + " - " + result.penAway, skin, "dim")).row();
        } else if (result.extraTime) {
            root.add(new Label("Dimenangkan di perpanjangan waktu", skin, "dim")).row();
        }

        Table t = new Table();
        t.top().defaults().pad(1);
        if (result.goals.isEmpty()) {
            t.add(new Label("Tanpa gol", skin, "dim")).colspan(3).row();
        } else {
            for (MatchResult.Goal g : result.goals) {
                String line = g.minute + "'  " + g.name + (g.ownGoal ? " (bunuh diri)" : "");
                t.add(new Label(g.team == 0 ? line : "", skin)).width(150).right();
                t.add(new Label("(gol)", skin, "dim")).width(150).center();
                t.add(new Label(g.team == 1 ? line : "", skin)).width(150).left().row();
            }
        }
        t.add(new Label("", skin)).colspan(3).height(6).row();
        float p0 = result.stats[0].possession, p1 = result.stats[1].possession;
        int pa = (int) Math.round(100f * p0 / Math.max(1f, p0 + p1));
        row(t, pa + "%", "Penguasaan bola", (100 - pa) + "%");
        row(t, result.stats[0].shots, "Tembakan", result.stats[1].shots);
        row(t, result.stats[0].onTarget, "Tepat sasaran", result.stats[1].onTarget);
        row(t, result.stats[0].passes, "Umpan", result.stats[1].passes);
        row(t, result.stats[0].tackles, "Rebut bola", result.stats[1].tackles);
        row(t, result.stats[0].saves, "Penyelamatan", result.stats[1].saves);
        row(t, result.stats[0].corners, "Tendangan sudut", result.stats[1].corners);

        ScrollPane sp = new ScrollPane(t, skin);
        sp.setFadeScrollBars(false);
        sp.setScrollingDisabled(true, false);
        root.add(sp).grow().padTop(6).row();

        String reward = cfg.rewards && !result.forfeit
                ? "Hadiah: +" + result.coinsEarned + " koin   (" + result.rewardNote + ")" : "";
        root.add(new Label(reward, skin, "dim")).padTop(4).minHeight(16).row();
        root.add(button("Lanjut", new Runnable() {
            @Override public void run() { go(next.next(game, result)); }
        })).width(180).height(36).padTop(4);
    }

    private void row(Table t, Object a, String label, Object b) {
        t.add(new Label(String.valueOf(a), skin)).width(150).right();
        t.add(new Label(label, skin, "dim")).width(150).center();
        t.add(new Label(String.valueOf(b), skin)).width(150).left().row();
    }

    @Override
    protected void onBack() {
        go(next.next(game, result));
    }
}
