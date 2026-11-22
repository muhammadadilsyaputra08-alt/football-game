package com.cozypixel.soccer.engine;

import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Data formasi (setara lua_scripts/formations.lua, ditanam di Java supaya tidak butuh LuaJ).
 * x: 0 = gawang sendiri, 1 = gawang lawan. y: 0 = bawah layar, 1 = atas layar
 * (dilihat dari tim yang menyerang ke kanan).
 */
public final class Formations {
    private Formations() {}

    public static final String[] NAMES = {"4-3-3", "4-4-2", "4-2-3-1", "3-5-2"};

    public static final class Slot {
        public final String role;
        public final float x, y;
        Slot(String role, float x, float y) { this.role = role; this.x = x; this.y = y; }
    }

    public static List<Slot> get(String name) {
        List<Slot> l = new ArrayList<>();
        l.add(new Slot("GK", 0.08f, 0.50f));
        if ("4-4-2".equals(name)) {
            l.add(new Slot("RB", 0.22f, 0.18f)); l.add(new Slot("CB", 0.18f, 0.38f));
            l.add(new Slot("CB", 0.18f, 0.62f)); l.add(new Slot("LB", 0.22f, 0.82f));
            l.add(new Slot("CM", 0.40f, 0.28f)); l.add(new Slot("CM", 0.40f, 0.72f));
            l.add(new Slot("CM", 0.52f, 0.40f)); l.add(new Slot("CM", 0.52f, 0.60f));
            l.add(new Slot("ST", 0.78f, 0.38f)); l.add(new Slot("ST", 0.78f, 0.62f));
        } else if ("4-2-3-1".equals(name)) {
            l.add(new Slot("RB", 0.22f, 0.18f)); l.add(new Slot("CB", 0.18f, 0.38f));
            l.add(new Slot("CB", 0.18f, 0.62f)); l.add(new Slot("LB", 0.22f, 0.82f));
            l.add(new Slot("CDM", 0.35f, 0.38f)); l.add(new Slot("CDM", 0.35f, 0.62f));
            l.add(new Slot("CAM", 0.58f, 0.50f)); l.add(new Slot("RW", 0.65f, 0.22f));
            l.add(new Slot("ST", 0.82f, 0.50f)); l.add(new Slot("LW", 0.65f, 0.78f));
        } else if ("3-5-2".equals(name)) {
            l.add(new Slot("CB", 0.18f, 0.30f)); l.add(new Slot("CB", 0.16f, 0.50f));
            l.add(new Slot("CB", 0.18f, 0.70f)); l.add(new Slot("CM", 0.38f, 0.22f));
            l.add(new Slot("CDM", 0.35f, 0.50f)); l.add(new Slot("CM", 0.38f, 0.78f));
            l.add(new Slot("CAM", 0.55f, 0.40f)); l.add(new Slot("CAM", 0.55f, 0.60f));
            l.add(new Slot("ST", 0.80f, 0.38f)); l.add(new Slot("ST", 0.80f, 0.62f));
        } else { // 4-3-3 (default)
            l.add(new Slot("RB", 0.22f, 0.18f)); l.add(new Slot("CB", 0.18f, 0.38f));
            l.add(new Slot("CB", 0.18f, 0.62f)); l.add(new Slot("LB", 0.22f, 0.82f));
            l.add(new Slot("CDM", 0.35f, 0.50f)); l.add(new Slot("CM", 0.48f, 0.35f));
            l.add(new Slot("CAM", 0.58f, 0.50f)); l.add(new Slot("RW", 0.72f, 0.20f));
            l.add(new Slot("ST", 0.82f, 0.50f)); l.add(new Slot("LW", 0.72f, 0.80f));
        }
        return l;
    }

    private static int group(String pos) {
        switch (pos) {
            case "GK": return 0;
            case "RB": case "LB": case "CB": return 1;
            case "CDM": case "CM": case "CAM": return 2;
            default: return 3; // RW, LW, ST
        }
    }

    /** Pengali performa bila pemain dipasang di luar posisi aslinya (balance.lua position_penalty). */
    public static float penalty(String playerPos, String slotRole) {
        if (playerPos == null || playerPos.equals(slotRole)) return 1f;
        if (group(playerPos) == group(slotRole)) return 0.92f;
        if (Math.abs(group(playerPos) - group(slotRole)) == 1 && group(playerPos) != 0 && group(slotRole) != 0) return 0.8f;
        return 0.65f;
    }

    /** Cocokkan 11 pemain ke slot formasi: GK dulu, lalu posisi persis, lalu satu kelompok, lalu sisanya. */
    public static Player[] assign(List<Player> xi, List<Slot> slots) {
        Player[] out = new Player[slots.size()];
        List<Player> pool = new ArrayList<>(xi);
        for (int i = 0; i < slots.size(); i++) {
            if (!"GK".equals(slots.get(i).role)) continue;
            for (Player p : pool) if (p.isGK()) { out[i] = p; pool.remove(p); break; }
        }
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < slots.size(); i++) {
                if (out[i] != null) continue;
                String role = slots.get(i).role;
                for (Player p : pool) {
                    if (p.isGK() && !"GK".equals(role)) continue;
                    boolean ok = pass == 0 ? basePos(p).equals(role)
                            : pass == 1 ? group(basePos(p)) == group(role) : true;
                    if (ok) { out[i] = p; pool.remove(p); break; }
                }
            }
        }
        return out;
    }

    /** "CB1"/"CB2" -> posisi dasar sudah disimpan di Player.position ("CB"). */
    private static String basePos(Player p) { return p.position == null ? "CM" : p.position; }
}
