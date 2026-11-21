package com.cozypixel.soccer.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.cozypixel.soccer.model.SaveData;

/**
 * Local save system — JSON di internal storage.
 * Offline, tanpa login, sesuai GDD.
 */
public class SaveManager {
    private static final String SAVE_FILE = "cozysoccer_save.json";
    private static final Json json = new Json();

    static {
        json.setOutputType(JsonWriter.OutputType.json);
        json.setTypeName(null);
    }

    public static void save(SaveData data) {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            String text = json.prettyPrint(data);
            file.writeString(text, false);
            Gdx.app.log("SaveManager", "Game saved successfully");
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to save: " + e.getMessage());
        }
    }

    public static SaveData load() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (!file.exists()) {
                Gdx.app.log("SaveManager", "No save found, starting new game");
                return null;
            }
            String text = file.readString();
            SaveData data = json.fromJson(SaveData.class, text);
            Gdx.app.log("SaveManager", "Game loaded");
            return data;
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load: " + e.getMessage());
            return null;
        }
    }

    public static void deleteSave() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
