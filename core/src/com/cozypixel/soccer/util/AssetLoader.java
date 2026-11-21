package com.cozypixel.soccer.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Memuat asset dari pack CozyPixelSoccer.
 * Sprite sheet 32x32, filter Nearest.
 */
public class AssetLoader {
    private final AssetManager manager;
    private final ObjectMap<String, TextureRegion[][]> spriteSheets = new ObjectMap<>();

    public static final String BASE = "CozyPixelSoccer/";

    public AssetLoader(AssetManager manager) {
        this.manager = manager;
    }

    public void queueQueue() {
        // Stadium
        manager.load(BASE + "Environment/Stadium/Stadium_Day.png", Texture.class);
        // Field tiles
        manager.load(BASE + "Environment/Football_Field/Grass_Base.png", Texture.class);
        manager.load(BASE + "Environment/Football_Field/Center_Circle.png", Texture.class);
        // Ball
        manager.load(BASE + "Objects/Football/Football_16x16.png", Texture.class);
        manager.load(BASE + "Objects/Football/Football_16x16_Spin_4f.png", Texture.class);
        // UI Main Menu
        manager.load(BASE + "UI/Main_Menu/btn_play.png", Texture.class);
        manager.load(BASE + "UI/Main_Menu/btn_training.png", Texture.class);
        manager.load(BASE + "UI/Main_Menu/btn_tournament.png", Texture.class);
        manager.load(BASE + "UI/Main_Menu/btn_team.png", Texture.class);
        manager.load(BASE + "UI/Main_Menu/btn_shop.png", Texture.class);
        manager.load(BASE + "UI/Main_Menu/menu_panel_9slice.png", Texture.class);
        // HUD
        manager.load(BASE + "UI/Gameplay_HUD/joystick_base.png", Texture.class);
        manager.load(BASE + "UI/Gameplay_HUD/btn_shoot.png", Texture.class);
        manager.load(BASE + "UI/Gameplay_HUD/btn_sprint.png", Texture.class);

        // Default players (contoh ST)
        manager.load(BASE + "Characters/Default_Players/ST/Player_ST.png", Texture.class);
        manager.load(BASE + "Characters/Default_Players/GK/Player_GK.png", Texture.class);

        // Recruit portraits
        manager.load(BASE + "Characters/Recruitable_Players/Rafael/Player_Rafael_portrait.png", Texture.class);
        manager.load(BASE + "Characters/Recruitable_Players/Leon/Player_Leon_portrait.png", Texture.class);
        manager.load(BASE + "Characters/Recruitable_Players/Kai/Player_Kai_portrait.png", Texture.class);
    }

    public void finishLoading() {
        manager.finishLoading();
        // Set filter nearest untuk semua texture pixel art
        for (Texture t : manager.getAll(Texture.class, new com.badlogic.gdx.utils.Array<>())) {
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    public Texture getTexture(String path) {
        return manager.get(BASE + path, Texture.class);
    }

    public boolean isLoaded() {
        return manager.isFinished();
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public void update() {
        manager.update();
    }
}
