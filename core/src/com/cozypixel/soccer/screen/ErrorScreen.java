package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Layar diagnostik: menampilkan stack trace di layar (alih-alih force close)
 * dan menyimpannya ke crash.log agar penyebab error bisa dibaca di perangkat.
 */
public class ErrorScreen extends ScreenAdapter {
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final String text;

    public ErrorScreen(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        text = "PIXEL SOCCER - ERROR\n\n" + sw;
        font.getData().setScale(1.1f);
        try {
            Gdx.files.local("crash.log").writeString(text, false);
        } catch (Throwable ignored) { }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.25f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, text, 12, Gdx.graphics.getHeight() - 12,
                Gdx.graphics.getWidth() - 24, 8, true);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
