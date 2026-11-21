package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Training — tiap drill memberi koin + peluang +1 statistik pemain XI yang relevan.
 */
public class TrainingScreen extends BaseScreen {
    // {nama, stat, tipe}
    private static final String[][] DRILLS = {
        {"Shooting Practice", "Shooting", "out"},
        {"Passing Practice", "Passing", "out"},
        {"Dribbling Course", "Dribbling", "out"},
        {"Goalkeeper Practice", "Reflex", "gk"},
        {"Free Kick", "Shooting", "out"},
        {"Penalty Practice", "Shooting", "out"}
    };

    public TrainingScreen(PixelSoccerGame game) {
        super(game, new Color(0.2f, 0.4f, 0.25f, 1f));
        frame("TRAINING GROUND");
        refresh();
    }

    private void refresh() {
        infoLabel.setText("Koin: " + game.saveData.club.coins + "   (tanpa energi / tiket)");
        content.clear();
        for (final String[] d : DRILLS) {
            content.add(button(d[0] + "   [" + d[1] + "]", new Runnable() {
                @Override public void run() { runDrill(d[0], d[1], "gk".equals(d[2])); }
            })).width(320).height(38).row();
        }
    }

    private void runDrill(String name, String stat, boolean gk) {
        int coins = 20 + MathUtils.random(0, 15);
        game.saveData.club.coins += coins;
        String msg = name + " selesai! +" + coins + " koin";

        List<Player> pool = new ArrayList<>();
        for (Player p : game.playerTeam.getStartingPlayers()) {
            if (p.isGK() == gk) pool.add(p);
        }
        if (!pool.isEmpty() && MathUtils.randomBoolean(0.4f)) {
            Player target = pool.get(MathUtils.random(pool.size() - 1));
            target.setStat(stat, target.getStat(stat) + 1);
            msg += "  |  " + target.name + " " + stat + " +1";
        }
        game.saveGame();
        refresh();
        say(msg);
    }
}
