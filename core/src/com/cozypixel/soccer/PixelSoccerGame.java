package com.cozypixel.soccer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.assets.AssetManager;
import com.cozypixel.soccer.model.SaveData;
import com.cozypixel.soccer.model.Team;
import com.cozypixel.soccer.model.Player;
import com.cozypixel.soccer.util.SaveManager;
import com.cozypixel.soccer.util.AssetLoader;
import com.cozypixel.soccer.screen.MainMenuScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point game — LibGDX Game class.
 * Mengelola screen, asset, dan save data global.
 */
public class PixelSoccerGame extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assets;
    public SaveData saveData;
    public Team playerTeam;
    public AssetLoader assetLoader;

    // Resolusi virtual (landscape)
    public static final int V_WIDTH = 640;
    public static final int V_HEIGHT = 384;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // diganti pixel font nanti
        assets = new AssetManager();
        assetLoader = new AssetLoader(assets);

        // Load save atau buat baru
        saveData = SaveManager.load();
        if (saveData == null) {
            saveData = createNewGame();
            SaveManager.save(saveData);
        }

        playerTeam = buildTeamFromSave(saveData);

        // Mulai dari main menu
        setScreen(new MainMenuScreen(this));
    }

    private SaveData createNewGame() {
        SaveData data = new SaveData();
        data.club.name = "Cozy FC";
        data.club.coins = 500;

        // Load 11 default dari roster (hardcoded alpha, nanti dari Lua/JSON)
        List<Player> defaults = createDefaultPlayers();
        for (Player p : defaults) {
            data.ownedPlayers.add(p);
            data.startingLineup.add(p.id);
        }
        data.captainId = "ST";
        data.formation = "4-3-3";
        return data;
    }

    private List<Player> createDefaultPlayers() {
        List<Player> list = new ArrayList<>();

        // Data diambil dari roster.json / players.lua (disederhanakan di sini)
        list.add(makePlayer("GK", "Bima", "GK", 1, 72, 68, 70, 65, 60, 68, true));
        list.add(makePlayer("RB", "Raka", "RB", 2, 70, 35, 62, 55, 72, 68, false));
        list.add(makePlayer("CB1", "Dimas", "CB", 3, 58, 30, 55, 48, 78, 80, false));
        list.add(makePlayer("CB2", "Arga", "CB", 4, 60, 32, 58, 50, 76, 78, false));
        list.add(makePlayer("LB", "Fajar", "LB", 5, 72, 38, 65, 58, 70, 65, false));
        list.add(makePlayer("CDM", "Galih", "CDM", 6, 65, 48, 70, 62, 75, 72, false));
        list.add(makePlayer("CM", "Nando", "CM", 8, 68, 55, 75, 70, 60, 65, false));
        list.add(makePlayer("CAM", "Yoga", "CAM", 10, 72, 68, 78, 80, 42, 58, false));
        list.add(makePlayer("RW", "Satria", "RW", 7, 82, 65, 68, 78, 35, 55, false));
        list.add(makePlayer("ST", "Bagas", "ST", 9, 75, 78, 55, 70, 30, 72, false));
        list.add(makePlayer("LW", "Tio", "LW", 11, 80, 62, 70, 76, 32, 52, false));

        return list;
    }

    private Player makePlayer(String id, String name, String pos, int num,
                              float paceOrReflex, float shootOrHandling,
                              float passOrPos, float dribOrDive,
                              float defOrDist, float physical, boolean gk) {
        Player p = new Player(id, name, pos);
        p.number = num;
        p.rarity = "Common";
        p.maxLevel = 18;
        p.owned = true;
        if (gk) {
            p.setStat("Reflex", paceOrReflex);
            p.setStat("Handling", shootOrHandling);
            p.setStat("Positioning", passOrPos);
            p.setStat("Diving", dribOrDive);
            p.setStat("Distribution", defOrDist);
            p.setStat("Physical", physical);
        } else {
            p.setStat("Pace", paceOrReflex);
            p.setStat("Shooting", shootOrHandling);
            p.setStat("Passing", passOrPos);
            p.setStat("Dribbling", dribOrDive);
            p.setStat("Defense", defOrDist);
            p.setStat("Physical", physical);
        }
        return p;
    }

    private Team buildTeamFromSave(SaveData data) {
        Team team = new Team(data.club.name);
        team.formation = data.formation;
        team.ownedPlayers.addAll(data.ownedPlayers);
        team.startingXI.addAll(data.startingLineup);
        team.bench.addAll(data.bench);
        team.captainId = data.captainId;
        return team;
    }

    public void saveGame() {
        // Sync team -> saveData
        saveData.ownedPlayers = playerTeam.ownedPlayers;
        saveData.startingLineup = playerTeam.startingXI;
        saveData.bench = playerTeam.bench;
        saveData.formation = playerTeam.formation;
        saveData.captainId = playerTeam.captainId;
        SaveManager.save(saveData);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assets.dispose();
        if (getScreen() != null) getScreen().dispose();
    }
}
