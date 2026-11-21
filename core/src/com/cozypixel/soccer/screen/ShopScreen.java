package com.cozypixel.soccer.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop — recruitment. Leon (Rare GK), Rafael (Epic ST), Kai (Legendary CAM).
 */
public class ShopScreen extends BaseScreen {
    private final List<Player> recruits = createRecruits();

    public ShopScreen(PixelSoccerGame game) {
        super(game, new Color(0.25f, 0.2f, 0.3f, 1f));
        frame("RECRUITMENT SHOP");
        refresh();
    }

    private void refresh() {
        final int coins = game.saveData.club.coins;
        infoLabel.setText("Koin: " + coins + "   (dapat koin dari Training & Tournament)");
        content.clear();
        for (final Player r : recruits) {
            boolean owned = game.playerTeam.getPlayer(r.id) != null;
            boolean poor = coins < r.price;
            String tag = owned ? "[OWNED] " : poor ? "[KOIN KURANG] " : "RECRUIT ";
            String info = String.format("%s%s - %s\n%s | %s | MaxLv %d\nPassive: %s\nHarga: %d koin",
                    tag, r.name, r.title != null ? r.title : "",
                    r.rarity, r.position, r.maxLevel,
                    r.passive != null ? r.passive : "-", r.price);
            TextButton btn = button(info, new Runnable() {
                @Override public void run() { recruit(r); }
            });
            btn.setDisabled(owned || poor);
            content.add(btn).width(360).height(84).row();
        }
    }

    private void recruit(Player r) {
        if (game.playerTeam.recruit(r, game.saveData.club.coins)) {
            game.saveData.club.coins -= r.price;
            game.saveGame();
            refresh();
            say(r.name + " bergabung! (masuk bench, atur di menu Team)");
        } else {
            say("Gagal merekrut " + r.name);
        }
    }

    private List<Player> createRecruits() {
        List<Player> list = new ArrayList<>();

        Player leon = new Player("Leon", "Leon", "GK");
        leon.title = "The Guardian";
        leon.rarity = "Rare";
        leon.maxLevel = 18;
        leon.passive = "Last Hope";
        leon.price = 1200;
        leon.setStat("Reflex", 86);
        leon.setStat("Handling", 79);
        leon.setStat("Positioning", 78);
        leon.setStat("Diving", 82);
        leon.setStat("Distribution", 70);
        leon.setStat("Physical", 75);
        list.add(leon);

        Player rafael = new Player("Rafael", "Rafael", "ST");
        rafael.title = "The Sniper";
        rafael.rarity = "Epic";
        rafael.maxLevel = 20;
        rafael.passive = "Sniper";
        rafael.price = 2500;
        rafael.setStat("Pace", 82);
        rafael.setStat("Shooting", 91);
        rafael.setStat("Passing", 58);
        rafael.setStat("Dribbling", 75);
        rafael.setStat("Defense", 28);
        rafael.setStat("Physical", 70);
        list.add(rafael);

        Player kai = new Player("Kai", "Kai", "CAM");
        kai.title = "The Maestro";
        kai.rarity = "Legendary";
        kai.maxLevel = 20;
        kai.passive = "Playmaker";
        kai.price = 4000;
        kai.setStat("Pace", 78);
        kai.setStat("Shooting", 72);
        kai.setStat("Passing", 94);
        kai.setStat("Dribbling", 90);
        kai.setStat("Defense", 45);
        kai.setStat("Physical", 62);
        list.add(kai);

        return list;
    }
}
