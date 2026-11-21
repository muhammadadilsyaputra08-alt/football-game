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

/**
 * Mode Training — mini games: Shooting, Passing, Dribbling, GK, Free Kick, Penalty.
 * Tidak ada energi/tiket. XP kecil.
 */
public class TrainingScreen extends ScreenAdapter {
    private final PixelSoccerGame game;
    private Stage stage;
    private Skin skin;

    public TrainingScreen(PixelSoccerGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = com.cozypixel.soccer.util.SkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("TRAINING GROUND", skin)).pad(20).row();

        String[] drills = {
            "Shooting Practice", "Passing Practice", "Dribbling Course",
            "Goalkeeper Practice", "Free Kick", "Penalty Practice"
        };
        for (String d : drills) {
            TextButton btn = new TextButton(d, skin);
            final String drill = d;
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    // TODO: launch mini-game screen
                    Gdx.app.log("Training", "Start: " + drill);
                    // Untuk alpha, kasih XP dummy
                    game.saveData.progress.matchesPlayed++; // placeholder
                    game.saveGame();
                }
            });
            root.add(btn).width(220).pad(6).row();
        }

        TextButton back = new TextButton("Kembali", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).padTop(20);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.4f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
