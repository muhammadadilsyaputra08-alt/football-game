package com.cozypixel.soccer.entity;

import com.badlogic.gdx.math.Vector2;

/**
 * Bola — fisika sederhana (bukan full Box2D untuk alpha).
 */
public class Ball {
    public Vector2 pos = new Vector2(320, 192);
    public Vector2 vel = new Vector2();
    public float radius = 6f;
    public String ownerId;   // null = free
    public float spin = 0;

    private static final float FRICTION = 0.985f;
    private static final float BOUNCE = 0.6f;

    public void update(float dt, float pitchW, float pitchH) {
        if (ownerId != null) {
            // dibawa pemain — posisi di-set dari luar
            vel.setZero();
            return;
        }

        pos.mulAdd(vel, dt);
        vel.scl(FRICTION);

        // Boundary
        if (pos.x < radius) { pos.x = radius; vel.x *= -BOUNCE; }
        if (pos.x > pitchW - radius) { pos.x = pitchW - radius; vel.x *= -BOUNCE; }
        if (pos.y < radius) { pos.y = radius; vel.y *= -BOUNCE; }
        if (pos.y > pitchH - radius) { pos.y = pitchH - radius; vel.y *= -BOUNCE; }

        // Stop jika sangat lambat
        if (vel.len2() < 4f) vel.setZero();
    }

    public void kick(Vector2 direction, float power) {
        ownerId = null;
        vel.set(direction).nor().scl(power * 280f);
    }

    public void setOwner(String id, Vector2 playerPos) {
        ownerId = id;
        pos.set(playerPos);
    }

    public void release() {
        ownerId = null;
    }
}
