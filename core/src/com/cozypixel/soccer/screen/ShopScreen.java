package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop — recruitment dengan rotasi stok.
 * Leon (Rare GK), Rafael (Epic ST), Kai (Legendary CAM).
 */
public class ShopScreen extends ScreenAdapter {
    private final PixelSoccerGame game;
    private Stage stage;
    private Skin skin;

    public ShopScreen(PixelSoccerGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = com.cozypixel.soccer.util.SkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("RECRUITMENT SHOP", skin)).pad(12).row();
        root.add(new Label("Koin: " + game.saveData.club.coins, skin)).padBottom(10).row();

        List<Player> recruits = createRecruits();
        for (Player r : recruits) {
            boolean alreadyOwned = game.playerTeam.getPlayer(r.id) != null;
            String info = String.format("%s — %s\n%s · %s · MaxLv %d · %s\nPassive: %s\nHarga: %d koin",
                    r.name, r.title != null ? r.title : "",
                    r.rarity, r.position, r.maxLevel, r.job,
                    r.passive != null ? r.passive : "-", r.price);

            TextButton btn = new TextButton(
                    (alreadyOwned ? "[OWNED] " : "RECRUIT ") + info, skin);
            btn.setDisabled(alreadyOwned || game.saveData.club.coins < r.price);

            final Player recruit = r;
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    if (game.playerTeam.recruit(recruit, game.saveData.club.coins)) {
                        game.saveData.club.coins -= recruit.price;
                        game.saveGame();
                        Gdx.app.log("Shop", "Recruited " + recruit.name);
                        game.setScreen(new ShopScreen(game)); // refresh
                    }
                }
            });
            root.add(btn).width(360).height(70).pad(4).row();
        }

        TextButton back = new TextButton("Kembali", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).padTop(12);
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

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.25f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
