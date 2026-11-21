package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;

/**
 * Koleksi pemain + upgrade statistik (butuh koin).
 */
public class PlayersScreen extends BaseScreen {

    public PlayersScreen(PixelSoccerGame game) {
        super(game, new Color(0.22f, 0.22f, 0.3f, 1f));
        frame("PLAYERS");
        refresh();
    }

    private void refresh() {
        infoLabel.setText("Koin: " + game.saveData.club.coins);
        content.clear();
        for (final Player p : game.playerTeam.ownedPlayers) {
            String passive = p.passive != null ? p.passive : "-";
            String line = String.format("%s  Lv%d/%d  OVR %.0f  %s\nPassive: %s",
                    p.name, p.level, p.maxLevel, p.getOverall(), p.position, passive);
            Table row = new Table();
            row.add(new Label(line, skin)).width(260).left();

            final int cost = p.getUpgradeCost();
            boolean max = !p.canLevelUp();
            TextButton up = button(max ? "MAX" : "Upgrade " + cost, new Runnable() {
                @Override public void run() { upgrade(p, cost); }
            });
            up.setDisabled(max || game.saveData.club.coins < cost);
            row.add(up).width(120).height(36).padLeft(8);
            content.add(row).row();
        }
    }

    private void upgrade(Player p, int cost) {
        if (game.saveData.club.coins < cost || !p.canLevelUp()) return;
        game.saveData.club.coins -= cost;
        if (p.isGK()) {
            p.setStat("Reflex", p.getStat("Reflex") + 2);
        } else {
            p.setStat("Shooting", p.getStat("Shooting") + 2);
            p.setStat("Pace", p.getStat("Pace") + 1);
        }
        p.level++;
        game.saveGame();
        refresh();
        say(p.name + " naik ke Lv" + p.level + " (-" + cost + " koin)");
    }
}
