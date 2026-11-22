package com.cozypixel.soccer.util;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Sprite sheet pemain: sel 32x32, 4 kolom, baris didefinisikan di file JSON pack
 * (nama, jumlah frame, fps, loop). Lihat Documentation/README.md di asset pack.
 */
public final class SpriteSheet {
    public static final class Row {
        public String name;
        public int row, frames;
        public float fps;
        public boolean loop;
        TextureRegion[] regions;
    }

    public final Texture texture;
    private final int cw, ch;
    private final ObjectMap<String, Row> rows = new ObjectMap<>();

    public SpriteSheet(Texture texture, FileHandle json) {
        this.texture = texture;
        JsonValue root = new JsonReader().parse(json);
        JsonValue cell = root.get("cell");
        cw = cell != null ? cell.getInt(0) : 32;
        ch = cell != null ? cell.getInt(1) : 32;
        for (JsonValue v = root.get("rows").child; v != null; v = v.next) {
            Row r = new Row();
            r.name = v.getString("name");
            r.row = v.getInt("row");
            r.frames = v.getInt("frames");
            r.fps = v.getFloat("fps", 8f);
            r.loop = v.getBoolean("loop", true);
            r.regions = new TextureRegion[r.frames];
            for (int i = 0; i < r.frames; i++) {
                r.regions[i] = new TextureRegion(texture, i * cw, r.row * ch, cw, ch);
            }
            rows.put(r.name, r);
        }
    }

    public boolean has(String name) { return rows.containsKey(name); }

    /** Frame pada waktu t (detik); loop sesuai definisi baris. null bila baris tidak ada. */
    public TextureRegion frame(String name, float t) {
        Row r = rows.get(name);
        if (r == null) return null;
        int idx = (int) (Math.max(0f, t) * r.fps);
        idx = r.loop ? idx % r.frames : Math.min(idx, r.frames - 1);
        return r.regions[idx];
    }
}
