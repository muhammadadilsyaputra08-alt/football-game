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
 * Tournament mode — Beginner Cup (4), Cozy League Cup (8), Grand Pixel Cup (8).
 * Knockout, reward koin + bonus.
 */
public class TournamentScreen extends ScreenAdapter {
    private final PixelSoccerGame game;
    private Stage stage;
    private Skin skin;

    private static final String[] CUPS = {
        "Beginner Cup", "Cozy League Cup", "Grand Pixel Cup"
    };
    private static final int[] TEAM_COUNTS = {4, 8, 8};

    public TournamentScreen(PixelSoccerGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = com.cozypixel.soccer.util.SkinFactory.create();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("TOURNAMENT", skin)).pad(16).row();
        root.add(new Label("Koin: " + game.saveData.club.coins, skin)).padBottom(12).row();

        for (int i = 0; i < CUPS.length; i++) {
            final String cup = CUPS[i];
            final int idx = i;
            boolean unlocked = Boolean.TRUE.equals(game.saveData.tournamentsUnlocked.get(cup));
            String best = game.saveData.bestResults.getOrDefault(cup, "-");

            TextButton btn = new TextButton(
                    cup + " (" + TEAM_COUNTS[i] + " tim)\nBest: " + best + (unlocked ? "" : "  [LOCKED]"),
                    skin);
            btn.setDisabled(!unlocked);
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    if (unlocked) {
                        // Launch match / bracket
                        Gdx.app.log("Tournament", "Enter: " + cup);
                        // Alpha: simulasi menang + reward
                        int reward = (idx == 0) ? 500 : (idx == 1) ? 1200 : 2500;
                        game.saveData.club.coins += reward;
                        game.saveData.bestResults.put(cup, "Champion");
                        game.saveData.progress.trophies++;
                        // Unlock next
                        if (idx < CUPS.length - 1) {
                            game.saveData.tournamentsUnlocked.put(CUPS[idx + 1], true);
                        }
                        game.saveGame();
                        // Refresh screen
                        game.setScreen(new TournamentScreen(game));
                    }
                }
            });
            root.add(btn).width(280).height(50).pad(6).row();
        }

        TextButton back = new TextButton("Kembali", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).padTop(16);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0.18f, 0.28f, 0.38f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
