package com.cozypixel.soccer.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.cozypixel.soccer.engine.MatchEngine;

import java.util.Random;

/**
 * Melukis lapangan + tribun 640x384 satu kali ke satu tekstur: rumput belang, garis lapangan,
 * penonton, papan iklan (Ad_Board_*), bunting, pintu masuk stadion dari asset pack.
 * Koordinat pixmap y ke bawah; dunia y ke atas => py = H - wy.
 */
final class StadiumPainter {
    private static final int W = 640, H = 384;
    private static final Color LINE = Color.valueOf("f6f0d8");
    private static final Color GRASS_A = Color.valueOf("8fa35c");
    private static final Color GRASS_B = Color.valueOf("869a55");
    private static final Color GRASS_DARK = Color.valueOf("6c8045");
    private static final Color GRASS_LIGHT = Color.valueOf("a8b96e");
    private static final Color STANDS = Color.valueOf("6b5440");
    private static final Color STANDS_DARK = Color.valueOf("4d2c17");

    private StadiumPainter() {}

    static Texture paint(Art art) {
        Pixmap pm = new Pixmap(W, H, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.SourceOver);
        Random r = new Random(7);

        // ---- tribun ----
        pm.setColor(STANDS);
        pm.fill();
        pm.setColor(STANDS_DARK);
        for (int y = 0; y < H; y += 6) pm.fillRectangle(0, y + 5, W, 1);
        Color[] shirts = {Color.valueOf("f4893d"), Color.valueOf("fba74e"), Color.valueOf("fff4b0"),
                Color.valueOf("677a5d"), Color.valueOf("c64e2c"), Color.valueOf("ef6544"),
                Color.valueOf("ebda8d"), Color.valueOf("a4a51e"), Color.valueOf("465544"), Color.valueOf("83391d")};
        Color[] skins = {Color.valueOf("fcba62"), Color.valueOf("d98a4e"), Color.valueOf("f0c090"), Color.valueOf("a8643a")};
        for (int y = 0; y < H; y += 6) {
            for (int x = 0; x < W; x += 5) {
                boolean field = x >= 16 && x < 624 && y >= 40 && y < 340;
                if (field) continue;
                if (r.nextInt(100) < 9) continue;
                int ox = x + r.nextInt(2);
                pm.setColor(skins[r.nextInt(skins.length)]);
                pm.fillRectangle(ox + 1, y, 3, 3);
                pm.setColor(shirts[r.nextInt(shirts.length)]);
                pm.fillRectangle(ox, y + 3, 5, 3);
            }
        }

        // ---- rumput ----
        int gx0 = 16, gx1 = 624, gy0 = 42, gy1 = 338;   // area rumput (pixmap y)
        for (int x = gx0; x < gx1; x += 32) {
            pm.setColor(((x - gx0) / 32) % 2 == 0 ? GRASS_A : GRASS_B);
            pm.fillRectangle(x, gy0, Math.min(32, gx1 - x), gy1 - gy0);
        }
        for (int i = 0; i < 520; i++) {
            pm.setColor(GRASS_DARK);
            pm.drawPixel(gx0 + r.nextInt(gx1 - gx0), gy0 + r.nextInt(gy1 - gy0));
        }
        for (int i = 0; i < 140; i++) {
            pm.setColor(GRASS_LIGHT);
            pm.drawPixel(gx0 + r.nextInt(gx1 - gx0), gy0 + r.nextInt(gy1 - gy0));
        }

        // ---- garis lapangan ----
        int gl = (int) MatchEngine.GL, gr = (int) MatchEngine.GR;
        int top = H - (int) MatchEngine.TT, bot = H - (int) MatchEngine.TB;   // py garis samping
        int cy = H - (int) MatchEngine.CY, cx = (int) MatchEngine.CX;
        pm.setColor(LINE);
        pm.drawRectangle(gl, top, gr - gl, bot - top);                         // batas
        pm.fillRectangle(cx, top, 1, bot - top);                               // tengah
        circle(pm, cx, cy, 38, 0, 360, -1);
        pm.fillRectangle(cx - 1, cy - 1, 3, 3);
        for (int s = 0; s < 2; s++) {
            boolean left = s == 0;
            int dir = left ? 1 : -1;
            int gline = left ? gl : gr;
            // kotak penalti 66 x 152, kotak gawang 22 x 80
            box(pm, gline, cy - 76, 66, 152, dir);
            box(pm, gline, cy - 40, 22, 80, dir);
            int spot = gline + dir * 44;
            pm.fillRectangle(spot - 1, cy - 1, 3, 3);
            // busur D di luar kotak penalti
            int boxEdge = gline + dir * 66;
            for (int a = -90; a <= 90; a++) {
                double rad = Math.toRadians(a);
                int px = (int) Math.round(spot + dir * 28 * Math.cos(rad));
                int py = (int) Math.round(cy + 28 * Math.sin(rad));
                if ((px - boxEdge) * dir > 0) pm.drawPixel(px, py);
            }
        }
        // busur sudut
        corner(pm, gl, top, 1, 1); corner(pm, gr, top, -1, 1);
        corner(pm, gl, bot, 1, -1); corner(pm, gr, bot, -1, -1);

        // ---- papan iklan ----
        String[] ads = {"COZY", "KOPI", "SOTO", "PIXEL", "GOAL", "TEH", "FC"};
        Pixmap[] boards = new Pixmap[ads.length];
        for (int i = 0; i < ads.length; i++) boards[i] = art.pixmap("Environment/Decorations/Ad_Board_" + ads[i] + ".png");
        for (int i = 0, x = 16; x < 624; i++, x += 48) {
            pm.drawPixmap(boards[i % ads.length], x, 42);
            pm.drawPixmap(boards[(i + 3) % ads.length], x, 328);
        }
        for (Pixmap b : boards) b.dispose();

        // ---- dekorasi: bunting & pintu masuk & semak ----
        Pixmap bunting = art.pixmap("Environment/Decorations/Bunting.png");
        for (int x = 0; x < W; x += bunting.getWidth()) pm.drawPixmap(bunting, x, 28);
        bunting.dispose();
        Pixmap ent = art.pixmap("Environment/Decorations/Stadium_Entrance.png");
        pm.drawPixmap(ent, cx - ent.getWidth() / 2, 342);
        ent.dispose();
        Pixmap bush = art.pixmap("Environment/Decorations/Bush.png");
        pm.drawPixmap(bush, 0, 0); pm.drawPixmap(bush, W - bush.getWidth(), 0);
        pm.drawPixmap(bush, 0, H - bush.getHeight()); pm.drawPixmap(bush, W - bush.getWidth(), H - bush.getHeight());
        bush.dispose();

        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        return t;
    }

    private static void box(Pixmap pm, int gline, int y, int depth, int h, int dir) {
        int x = dir > 0 ? gline : gline - depth;
        pm.drawRectangle(x, y, depth + 1, h);
    }

    private static void circle(Pixmap pm, int cx, int cy, int rad, int a0, int a1, int unused) {
        for (int a = a0; a < a1; a++) {
            double r = Math.toRadians(a);
            pm.drawPixel((int) Math.round(cx + rad * Math.cos(r)), (int) Math.round(cy + rad * Math.sin(r)));
        }
    }

    private static void corner(Pixmap pm, int x, int y, int dx, int dy) {
        for (int a = 0; a <= 90; a += 2) {
            double r = Math.toRadians(a);
            pm.drawPixel((int) Math.round(x + dx * 6 * Math.cos(r)), (int) Math.round(y + dy * 6 * Math.sin(r)));
        }
    }
}
