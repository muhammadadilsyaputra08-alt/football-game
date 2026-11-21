package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;
import com.cozypixel.soccer.model.Team;

/**
 * Team — formasi, Starting XI (tap = jadikan kapten), bench (tombol "Masuk XI").
 */
public class TeamScreen extends BaseScreen {
    private static final String[] FORMATIONS = {"4-3-3", "4-4-2", "4-2-3-1", "3-5-2"};

    public TeamScreen(PixelSoccerGame game) {
        super(game, new Color(0.2f, 0.3f, 0.25f, 1f));
        frame("TEAM");
        refresh();
    }

    private void refresh() {
        final Team team = game.playerTeam;
        Player cap = team.getPlayer(team.captainId);
        infoLabel.setText("Formasi " + team.formation + "   |   Kapten: " + (cap != null ? cap.name : "-"));
        content.clear();

        Table formRow = new Table();
        for (final String f : FORMATIONS) {
            boolean current = f.equals(team.formation);
            formRow.add(button(current ? "* " + f + " *" : f, new Runnable() {
                @Override public void run() {
                    team.formation = f;
                    game.saveGame();
                    say("Formasi diubah ke " + f);
                    refresh();
                }
            })).width(110).height(32).pad(3);
        }
        content.add(formRow).row();

        content.add(new Label("Starting XI  (tap pemain = jadikan kapten)", skin, "dim")).padTop(6).row();
        for (final Player p : team.getStartingPlayers()) {
            boolean isCap = p.id.equals(team.captainId);
            String line = String.format("#%d %s (%s)  OVR %.0f  Lv%d%s",
                    p.number, p.name, p.position, p.getOverall(), p.level, isCap ? "  (C)" : "");
            content.add(button(line, new Runnable() {
                @Override public void run() {
                    team.captainId = p.id;
                    game.saveGame();
                    say(p.name + " menjadi kapten");
                    refresh();
                }
            })).width(380).height(28).row();
        }

        if (!team.bench.isEmpty()) {
            content.add(new Label("Bench", skin, "dim")).padTop(8).row();
            for (String id : team.bench) {
                final Player b = team.getPlayer(id);
                if (b == null) continue;
                Table row = new Table();
                row.add(new Label(String.format("%s (%s)  OVR %.0f  Lv%d", b.name, b.position, b.getOverall(), b.level), skin))
                        .width(250).left();
                row.add(button("Masuk XI", new Runnable() {
                    @Override public void run() { benchIn(b); }
                })).width(110).height(28);
                content.add(row).row();
            }
        }
    }

    /** Ganti starter berposisi sama; jika tidak ada, starter dengan OVR terendah dengan peran sama (GK/outfield). */
    private void benchIn(Player in) {
        Team t = game.playerTeam;
        Player out = null;
        for (Player s : t.getStartingPlayers()) {
            if (s.position != null && s.position.equals(in.position)) { out = s; break; }
        }
        if (out == null) {
            float low = Float.MAX_VALUE;
            for (Player s : t.getStartingPlayers()) {
                if (s.isGK() != in.isGK()) continue;
                if (s.getOverall() < low) { low = s.getOverall(); out = s; }
            }
        }
        if (out == null) {
            say("Tidak ada pemain yang cocok untuk diganti");
            return;
        }
        t.swapToStarting(in.id, out.id);
        if (out.id.equals(t.captainId)) t.captainId = in.id;
        game.saveGame();
        say(in.name + " masuk menggantikan " + out.name);
        refresh();
    }
}
