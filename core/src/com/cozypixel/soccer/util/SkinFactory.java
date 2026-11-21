package com.cozypixel.soccer.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Programmatic skin agar tidak bergantung pada uiskin.json eksternal.
 * Warna cozy / autumn sesuai STYLE_DNA.
 */
public final class SkinFactory {

    private SkinFactory() {}

    public static Skin create() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        // Warna cozy
        Color bg = new Color(0.25f, 0.18f, 0.12f, 0.92f);      // coklat gelap
        Color btnUp = new Color(0.85f, 0.55f, 0.25f, 1f);      // orange hangat
        Color btnDown = new Color(0.70f, 0.40f, 0.15f, 1f);
        Color btnOver = new Color(0.95f, 0.65f, 0.30f, 1f);
        Color text = new Color(1f, 0.96f, 0.85f, 1f);          // cream

        Texture white = createColorTexture(Color.WHITE);
        skin.add("white", white);

        NinePatch npUp = new NinePatch(white, 4, 4, 4, 4);
        npUp.setColor(btnUp);
        NinePatch npDown = new NinePatch(white, 4, 4, 4, 4);
        npDown.setColor(btnDown);
        NinePatch npOver = new NinePatch(white, 4, 4, 4, 4);
        npOver.setColor(btnOver);

        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.fontColor = text;
        tbs.up = new NinePatchDrawable(npUp);
        tbs.down = new NinePatchDrawable(npDown);
        tbs.over = new NinePatchDrawable(npOver);
        tbs.disabledFontColor = new Color(0.6f, 0.6f, 0.6f, 1f);
        skin.add("default", tbs);

        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = font;
        ls.fontColor = text;
        skin.add("default", ls);

        return skin;
    }

    private static Texture createColorTexture(Color color) {
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
