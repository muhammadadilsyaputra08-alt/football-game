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

/**
 * Players collection + upgrade statistik.
 */
public class PlayersScreen extends ScreenAdapter {
    private final PixelSoccerGame game;
    private Stage stage;
    private Skin skin;

    public PlayersScreen(PixelSoccerGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = com.cozypixel.soccer.util.SkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("PLAYERS  |  Koin: " + game.saveData.club.coins, skin)).pad(10).row();

        ScrollPane scroll = new ScrollPane(buildPlayerList(), skin);
        root.add(scroll).width(400).height(280).row();

        TextButton back = new TextButton("Kembali", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).padTop(10);
    }

    private Table buildPlayerList() {
        Table t = new Table();
        for (Player p : game.playerTeam.ownedPlayers) {
            String passive = p.passive != null ? p.passive : "-";
            String line = String.format("%s  Lv%d/%d  OVR%.0f  %s\nPassive: %s",
                    p.name, p.level, p.maxLevel, p.getOverall(), p.position, passive);
            Label lbl = new Label(line, skin);
            t.add(lbl).left().pad(4);

            TextButton up = new TextButton("Upgrade", skin);
            final Player target = p;
            up.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    int cost = target.getUpgradeCost();
                    if (game.saveData.club.coins >= cost && target.canLevelUp()) {
                        game.saveData.club.coins -= cost;
                        // Simple: naikkan shooting atau pace
                        if (target.isGK()) {
                            target.setStat("Reflex", target.getStat("Reflex") + 2);
                        } else {
                            target.setStat("Shooting", target.getStat("Shooting") + 2);
                            target.setStat("Pace", target.getStat("Pace") + 1);
                        }
                        target.level++;
                        game.saveGame();
                        game.setScreen(new PlayersScreen(game));
                    }
                }
            });
            t.add(up).pad(4).row();
        }
        return t;
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.22f, 0.22f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
