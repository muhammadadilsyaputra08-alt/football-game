package com.cozypixel.soccer.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Cache tekstur & sprite sheet dari asset pack CozyPixelSoccer. Semua filter Nearest (pixel art).
 * Dibuat sekali di PixelSoccerGame dan dipakai ulang lintas pertandingan.
 */
public final class Art implements Disposable {
    public static final String BASE = "CozyPixelSoccer/";

    private final ObjectMap<String, Texture> textures = new ObjectMap<>();
    private final ObjectMap<String, SpriteSheet> sheets = new ObjectMap<>();
    private final ObjectMap<String, TextureRegion[]> strips = new ObjectMap<>();
    private Texture white;
    private Texture stadium;

    public boolean exists(String rel) { return Gdx.files.internal(BASE + rel).exists(); }

    public Texture tex(String rel) {
        Texture t = textures.get(rel);
        if (t == null) {
            if (!exists(rel)) throw new GdxRuntimeException("Asset tidak ditemukan: " + BASE + rel);
            t = new Texture(Gdx.files.internal(BASE + rel));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textures.put(rel, t);
        }
        return t;
    }

    /** Tekstur 1x1 putih untuk menggambar kotak berwarna. */
    public Texture white() {
        if (white == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            white = new Texture(pm);
            pm.dispose();
        }
        return white;
    }

    /** Strip animasi horizontal dengan n frame sama lebar. */
    public TextureRegion[] strip(String rel, int frames) {
        String key = rel + "#" + frames;
        TextureRegion[] s = strips.get(key);
        if (s == null) {
            Texture t = tex(rel);
            int w = t.getWidth() / frames;
            s = new TextureRegion[frames];
            for (int i = 0; i < frames; i++) s[i] = new TextureRegion(t, i * w, 0, w, t.getHeight());
            strips.put(key, s);
        }
        return s;
    }

    public Pixmap pixmap(String rel) { return new Pixmap(Gdx.files.internal(BASE + rel)); }

    /** key = path tanpa ekstensi, mis. Characters/Default_Players/ST/Player_ST_away. */
    public SpriteSheet sheet(String key) {
        SpriteSheet s = sheets.get(key);
        if (s != null) return s;
        String png = key;
        if (!exists(png + ".png") && png.endsWith("_away")) png = png.substring(0, png.length() - 5);
        if (!exists(png + ".png")) throw new GdxRuntimeException("Sprite tidak ditemukan: " + key);
        String json = key.endsWith("_away") ? key.substring(0, key.length() - 5) : key;
        s = new SpriteSheet(tex(png + ".png"), Gdx.files.internal(BASE + json + ".json"));
        sheets.put(key, s);
        return s;
    }

    /** Tekstur stadion hasil lukis prosedural + dekorasi asset pack (dibuat sekali). */
    public Texture stadium() {
        if (stadium == null) stadium = StadiumPainter.paint(this);
        return stadium;
    }

    @Override
    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
        if (white != null) white.dispose();
        if (stadium != null) stadium.dispose();
        sheets.clear();
        strips.clear();
    }
}
