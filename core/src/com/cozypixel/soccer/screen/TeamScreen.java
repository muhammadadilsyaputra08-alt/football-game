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
 * Team management — starting XI, bench, formation, captain.
 */
public class TeamScreen extends ScreenAdapter {
    private final PixelSoccerGame game;
    private Stage stage;
    private Skin skin;

    public TeamScreen(PixelSoccerGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = com.cozypixel.soccer.util.SkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("TEAM — " + game.playerTeam.formation, skin)).pad(10).row();

        // Starting XI
        root.add(new Label("Starting XI:", skin)).left().row();
        for (String id : game.playerTeam.startingXI) {
            Player p = game.playerTeam.getPlayer(id);
            if (p == null) continue;
            String line = String.format("#%d %s (%s)  OVR %.0f  Lv%d",
                    p.number, p.name, p.position, p.getOverall(), p.level);
            root.add(new Label(line, skin)).left().padLeft(20).row();
        }

        // Bench
        if (!game.playerTeam.bench.isEmpty()) {
            root.add(new Label("Bench:", skin)).left().padTop(8).row();
            for (String id : game.playerTeam.bench) {
                Player p = game.playerTeam.getPlayer(id);
                if (p == null) continue;
                root.add(new Label("  " + p.name + " (" + p.position + ")", skin)).left().padLeft(20).row();
            }
        }

        // Formation selector (alpha simple)
        Table formRow = new Table();
        for (String f : new String[]{"4-3-3", "4-4-2", "4-2-3-1", "3-5-2"}) {
            TextButton b = new TextButton(f, skin);
            final String form = f;
            b.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    game.playerTeam.formation = form;
                    game.saveGame();
                    game.setScreen(new TeamScreen(game));
                }
            });
            formRow.add(b).pad(4);
        }
        root.add(formRow).padTop(12).row();

        TextButton back = new TextButton("Kembali", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).padTop(16);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.3f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
