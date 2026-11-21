package com.cozypixel.soccer.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Skin programatik (cozy pixel, palet STYLE_DNA). Dibuat SEKALI oleh PixelSoccerGame
 * dan dipakai semua layar. Berisi style: TextButton, Label (default/title/dim),
 * ScrollPane (wajib untuk layar Players/Team/Shop/Tournament).
 */
public final class SkinFactory {

    private SkinFactory() {}

    public static Skin create() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f);
        for (BitmapFont f : new BitmapFont[]{font, titleFont}) {
            for (TextureRegion r : f.getRegions()) {
                r.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
        }
        skin.add("default-font", font);
        skin.add("title-font", titleFont);

        Color outline = Color.valueOf("390c14");
        Color cream = Color.valueOf("fff4b0");
        Color gold = Color.valueOf("ffd98a");

        Texture white = solid(Color.WHITE);
        skin.add("white", white);

        // Tombol: normal / ditekan / nonaktif (abu-abu jelas)
        Drawable up = panel(skin, "btn_up", Color.valueOf("f4893d"), outline, Color.valueOf("fba74e"), Color.valueOf("c64e2c"));
        Drawable down = panel(skin, "btn_down", Color.valueOf("c64e2c"), outline, Color.valueOf("d96624"), Color.valueOf("83391d"));
        Drawable off = panel(skin, "btn_off", Color.valueOf("6b5d57"), outline, Color.valueOf("7d6f68"), Color.valueOf("4d4340"));

        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.fontColor = cream;
        tbs.downFontColor = Color.WHITE;
        tbs.disabledFontColor = Color.valueOf("b8aca3");
        tbs.up = up;
        tbs.down = down;
        tbs.over = up;
        tbs.disabled = off;
        tbs.pressedOffsetY = -1f;
        skin.add("default", tbs);

        Label.LabelStyle ls = new Label.LabelStyle(font, cream);
        skin.add("default", ls);
        skin.add("title", new Label.LabelStyle(titleFont, gold));
        skin.add("dim", new Label.LabelStyle(font, Color.valueOf("e8d8b0")));

        ScrollPane.ScrollPaneStyle sps = new ScrollPane.ScrollPaneStyle();
        sps.vScroll = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.25f));
        sps.vScrollKnob = skin.newDrawable("white", Color.valueOf("fba74e"));
        skin.add("default", sps);

        return skin;
    }

    /** 8x8 nine-patch bergaya pixel: outline gelap, highlight atas, bayangan bawah, sudut membulat. */
    private static Drawable panel(Skin skin, String name, Color fill, Color border, Color hi, Color sh) {
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(fill);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, 8, 8);
        pm.setColor(hi);
        pm.drawLine(2, 1, 5, 1);
        pm.setColor(sh);
        pm.drawLine(2, 6, 5, 6);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.drawPixel(0, 0);
        pm.drawPixel(7, 0);
        pm.drawPixel(0, 7);
        pm.drawPixel(7, 7);
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        skin.add(name + "_tex", t); // agar ikut di-dispose bersama Skin
        NinePatch np = new NinePatch(t, 2, 2, 2, 2);
        np.setPadLeft(10);
        np.setPadRight(10);
        np.setPadTop(6);
        np.setPadBottom(6);
        return new NinePatchDrawable(np);
    }

    private static Texture solid(Color color) {
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
