package com.cozypixel.soccer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Struktur save offline sesuai GDD section 15.
 * Disimpan sebagai JSON di local storage Android / desktop.
 */
public class SaveData {
    public Club club = new Club();
    public List<Player> ownedPlayers = new ArrayList<>();
    public List<String> startingLineup = new ArrayList<>();
    public List<String> bench = new ArrayList<>();
    public String formation = "4-3-3";
    public String captainId;

    public Map<String, Boolean> tournamentsUnlocked = new HashMap<>();
    public Map<String, String> bestResults = new HashMap<>();  // tournament -> "Champion" / "Runner-up"
    public List<String> completedTournaments = new ArrayList<>();

    public Progress progress = new Progress();

    // Shop rotation (4 pemain per refresh)
    public List<String> shopStock = new ArrayList<>();
    public long lastShopRefresh = 0;

    public static class Club {
        public String name = "Cozy FC";
        public int coins = 500;   // starting coins
    }

    public static class Progress {
        public int matchesPlayed = 0;
        public int matchesWon = 0;
        public int trophies = 0;
        public int goalsScored = 0;
        public int cleanSheets = 0;
    }

    public SaveData() {
        // Unlock Beginner Cup by default
        tournamentsUnlocked.put("Beginner Cup", true);
        tournamentsUnlocked.put("Cozy League Cup", false);
        tournamentsUnlocked.put("Grand Pixel Cup", false);
    }
}
