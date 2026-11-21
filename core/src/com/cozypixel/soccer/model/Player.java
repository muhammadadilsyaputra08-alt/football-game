package com.cozypixel.soccer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model pemain sesuai GDD.
 * Mendukung outfield stats dan GK stats.
 */
public class Player {
    public String id;
    public String name;
    public String title;          // e.g. "The Sniper"
    public String position;       // GK, RB, CB, LB, CDM, CM, CAM, RW, ST, LW
    public String job;
    public int number;
    public String rarity;         // Common, Rare, Epic, Legendary
    public int level = 1;
    public int maxLevel = 18;
    public String passive;        // null jika default tanpa passive
    public int price = 0;
    public boolean owned = true;

    // Visual
    public String hair = "black";
    public String style = "short";
    public String skin = "s3";

    // Stats (outfield)
    public Map<String, Float> stats = new HashMap<>();

    // Runtime
    public float stamina = 100f;
    public int matchesPlayed = 0;
    public float xp = 0;

    public Player() {
        // default empty stats
        stats.put("Pace", 60f);
        stats.put("Shooting", 50f);
        stats.put("Passing", 55f);
        stats.put("Dribbling", 55f);
        stats.put("Defense", 50f);
        stats.put("Physical", 60f);
    }

    public Player(String id, String name, String pos) {
        this();
        this.id = id;
        this.name = name;
        this.position = pos;
        this.job = pos;
    }

    public float getStat(String key) {
        return stats.getOrDefault(key, 50f);
    }

    public void setStat(String key, float value) {
        stats.put(key, Math.max(1f, Math.min(99f, value)));
    }

    public boolean isGK() {
        return "GK".equals(position);
    }

    public float getOverall() {
        if (isGK()) {
            float sum = getStat("Reflex") + getStat("Handling") + getStat("Positioning")
                    + getStat("Diving") + getStat("Distribution") + getStat("Physical");
            return sum / 6f;
        }
        float sum = getStat("Pace") + getStat("Shooting") + getStat("Passing")
                + getStat("Dribbling") + getStat("Defense") + getStat("Physical");
        return sum / 6f;
    }

    /** Biaya upgrade 1 poin atribut di level saat ini */
    public int getUpgradeCost() {
        if (level <= 5) return 50;
        if (level <= 10) return 100;
        if (level <= 15) return 175;
        return 250;
    }

    public boolean canLevelUp() {
        return level < maxLevel;
    }

    public void applyLevelUp(int attrPoints) {
        if (!canLevelUp()) return;
        level++;
        // caller decides how to distribute attrPoints
    }

    public Player copy() {
        Player p = new Player();
        p.id = id;
        p.name = name;
        p.title = title;
        p.position = position;
        p.job = job;
        p.number = number;
        p.rarity = rarity;
        p.level = level;
        p.maxLevel = maxLevel;
        p.passive = passive;
        p.price = price;
        p.owned = owned;
        p.hair = hair;
        p.style = style;
        p.skin = skin;
        p.stamina = stamina;
        p.matchesPlayed = matchesPlayed;
        p.xp = xp;
        p.stats = new HashMap<>(stats);
        return p;
    }
}
