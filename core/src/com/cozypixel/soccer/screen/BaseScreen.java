package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.cozypixel.soccer.PixelSoccerGame;

/**
 * Dasar semua layar: stage + skin bersama, tombol Back Android, rangka standar
 * (judul, info, area scroll, status, tombol Kembali) sehingga konten panjang
 * tidak lagi terpotong di luar layar.
 */
public abstract class BaseScreen extends ScreenAdapter {
    protected final PixelSoccerGame game;
    protected final Stage stage;
    protected final Skin skin;
    protected final Table root = new Table();
    protected final Table content = new Table();
    protected Label infoLabel;
    protected Label statusLabel;
    private final Color bg;

    protected BaseScreen(PixelSoccerGame game, Color bg) {
        this.game = game;
        this.skin = game.skin;
        this.bg = bg;
        this.stage = new Stage(new ExtendViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT), game.batch);
        root.setFillParent(true);
        stage.addActor(root);
    }

    /** Rangka standar; isi konten lewat {@link #content}. */
    protected void frame(String title) {
        root.top().pad(6);
        root.add(new Label(title, skin, "title")).padBottom(2).row();
        infoLabel = new Label("", skin, "dim");
        root.add(infoLabel).padBottom(4).row();

        content.top();
        content.defaults().pad(3);
        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        root.add(scroll).grow().row();

        statusLabel = new Label("", skin, "dim");
        root.add(statusLabel).padTop(4).minHeight(18).row();
        root.add(button("Kembali", new Runnable() {
            @Override public void run() { onBack(); }
        })).width(160).height(34).padTop(4).padBottom(2);
    }

    protected TextButton button(String text, final Runnable action) {
        TextButton b = new TextButton(text, skin);
        b.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.saveData != null && game.saveData.vibrationOn) {
                    try { Gdx.input.vibrate(15); } catch (Throwable ignored) { }
                }
                action.run();
            }
        });
        return b;
    }

    protected void say(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    protected void go(Screen next) {
        game.go(next);
    }

    protected void onBack() {
        go(new MainMenuScreen(game));
    }

    @Override
    public void show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    onBack();
                    return true;
                }
                return false;
            }
        }));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
