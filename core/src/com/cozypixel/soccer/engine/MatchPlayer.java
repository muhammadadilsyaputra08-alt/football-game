package com.cozypixel.soccer.engine;

import com.badlogic.gdx.math.Vector2;
import com.cozypixel.soccer.model.Player;

/** Pemain di lapangan (state runtime). Koordinat dunia: piksel, y ke atas. */
public final class MatchPlayer {
    public final int team;            // 0 = tuan rumah (manusia), 1 = tamu
    public final int index;           // slot formasi 0..10
    public final String role;         // GK, RB, CB, ...
    public final Player model;        // sumber stat (bisa dipakai ulang untuk XP)
    public final String name;
    public final int number;
    public final boolean gk;
    public final String spriteKey;    // path sheet relatif (tanpa BASE)
    public final float homeX, homeY;  // posisi formasi ternormalisasi (0..1) sudah dalam arah serang tim

    // stat efektif (sudah kena penalti posisi)
    public float pace, shoot, pass, drib, def, phys;
    public float reflex, handling, positioning, diving, distribution;

    // kinematika
    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();
    public final Vector2 face = new Vector2(1, 0);
    public final Vector2 desired = new Vector2();   // arah gerak keinginan (panjang 0..1)
    public boolean wantSprint;
    public boolean sprinting;
    public float stamina = 100f;

    // AI
    public float thinkTimer;
    public float tx, ty;              // target gerak
    public float targetSpeed = 1f;    // 0..1 pecahan kecepatan maks

    // timer
    public float noPickup, kickTimer, tackleTimer, tackleCd, stun, hold, diveTimer, celebrate;
    public float actionTime;          // waktu sejak aksi anim dimulai
    public boolean kickWasShot;
    public boolean fallen;
    public int diveDir;               // +1 ke atas layar, -1 ke bawah
    public float noSave;
    public float shotSeenAt = -1f;

    // charge tembakan (manusia)
    public boolean charging;
    public float charge;

    // statistik
    public int goals;

    public MatchPlayer(int team, int index, String role, Player model, String spriteKey, float hx, float hy) {
        this.team = team;
        this.index = index;
        this.role = role;
        this.model = model;
        this.name = model.name;
        this.number = model.number;
        this.gk = "GK".equals(role);
        this.spriteKey = spriteKey;
        this.homeX = hx;
        this.homeY = hy;
        float k = Formations.penalty(model.position, role);
        pace = model.getStat("Pace") * k;
        shoot = model.getStat("Shooting") * k;
        pass = model.getStat("Passing") * k;
        drib = model.getStat("Dribbling") * k;
        def = model.getStat("Defense") * k;
        phys = model.getStat("Physical") * k;
        reflex = model.getStat("Reflex") * (gk ? 1f : 0.6f);
        handling = model.getStat("Handling") * (gk ? 1f : 0.6f);
        positioning = model.getStat("Positioning") * (gk ? 1f : 0.6f);
        diving = model.getStat("Diving") * (gk ? 1f : 0.6f);
        distribution = model.getStat("Distribution") * (gk ? 1f : 0.6f);
    }

    public float maxSpeed() {
        if (gk) return 50f + reflex * 0.35f;
        return 52f + pace * 0.6f;
    }

    /** nama arah sprite: down, up, left, right */
    public String dirName() {
        if (Math.abs(face.x) >= Math.abs(face.y)) return face.x >= 0 ? "right" : "left";
        return face.y >= 0 ? "up" : "down";
    }
}
