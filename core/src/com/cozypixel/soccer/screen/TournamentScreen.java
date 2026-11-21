package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;

import java.util.List;

/**
 * Tournament (alpha: disimulasikan). Peluang menang bergantung rata-rata OVR starting XI.
 */
public class TournamentScreen extends BaseScreen {
    private static final String[] CUPS = {"Beginner Cup", "Cozy League Cup", "Grand Pixel Cup"};
    private static final int[] TEAM_COUNTS = {4, 8, 8};
    private static final int[] REQ_OVR = {55, 65, 75};
    private static final int[] REWARD = {500, 1200, 2500};

    public TournamentScreen(PixelSoccerGame game) {
        super(game, new Color(0.18f, 0.28f, 0.38f, 1f));
        frame("TOURNAMENT");
        refresh();
    }

    private float teamOvr() {
        List<Player> xi = game.playerTeam.getStartingPlayers();
        if (xi.isEmpty()) return 0f;
        float sum = 0f;
        for (Player p : xi) sum += p.getOverall();
        return sum / xi.size();
    }

    private void refresh() {
        infoLabel.setText(String.format("Koin: %d   |   OVR tim: %.0f   |   Trofi: %d",
                game.saveData.club.coins, teamOvr(), game.saveData.progress.trophies));
        content.clear();
        for (int i = 0; i < CUPS.length; i++) {
            final int idx = i;
            final String cup = CUPS[i];
            boolean unlocked = Boolean.TRUE.equals(game.saveData.tournamentsUnlocked.get(cup));
            String best = game.saveData.bestResults.containsKey(cup) ? game.saveData.bestResults.get(cup) : "-";
            String text = cup + " (" + TEAM_COUNTS[i] + " tim)\nSaran OVR " + REQ_OVR[i]
                    + "  |  Hadiah " + REWARD[i] + "\nBest: " + best + (unlocked ? "" : "   [LOCKED]");
            TextButton btn = button(text, new Runnable() {
                @Override public void run() { play(idx); }
            });
            btn.setDisabled(!unlocked);
            content.add(btn).width(340).height(72).row();
        }
    }

    private void play(int idx) {
        String cup = CUPS[idx];
        float chance = MathUtils.clamp(0.35f + (teamOvr() - REQ_OVR[idx]) / 40f, 0.10f, 0.95f);
        game.saveData.progress.matchesPlayed++;
        String msg;
        if (MathUtils.random() < chance) {
            game.saveData.club.coins += REWARD[idx];
            game.saveData.bestResults.put(cup, "Champion");
            game.saveData.progress.trophies++;
            game.saveData.progress.matchesWon++;
            if (!game.saveData.completedTournaments.contains(cup)) game.saveData.completedTournaments.add(cup);
            if (idx < CUPS.length - 1) game.saveData.tournamentsUnlocked.put(CUPS[idx + 1], true);
            msg = "JUARA " + cup + "! +" + REWARD[idx] + " koin";
        } else {
            int consolation = REWARD[idx] / 5;
            game.saveData.club.coins += consolation;
            if (!game.saveData.bestResults.containsKey(cup)) game.saveData.bestResults.put(cup, "Eliminated");
            msg = "Tersingkir di " + cup + ". Hiburan +" + consolation + " koin. Latih tim lalu coba lagi!";
        }
        game.saveGame();
        refresh();
        say(msg);
    }
}
