package com.cozypixel.soccer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tim klub (Cozy FC atau lawan AI).
 */
public class Team {
    public String name = "Cozy FC";
    public String formation = "4-3-3";
    public List<Player> ownedPlayers = new ArrayList<>();
    public List<String> startingXI = new ArrayList<>();   // player ids
    public List<String> bench = new ArrayList<>();
    public String captainId;

    public Team() {}

    public Team(String name) {
        this.name = name;
    }

    public Player getPlayer(String id) {
        for (Player p : ownedPlayers) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    public List<Player> getStartingPlayers() {
        List<Player> list = new ArrayList<>();
        for (String id : startingXI) {
            Player p = getPlayer(id);
            if (p != null) list.add(p);
        }
        return list;
    }

    public void setDefaultLineup(List<Player> defaults) {
        ownedPlayers.clear();
        startingXI.clear();
        bench.clear();
        for (Player p : defaults) {
            ownedPlayers.add(p);
            startingXI.add(p.id);
        }
        if (!startingXI.isEmpty()) {
            // default captain = ST atau CM
            for (String id : startingXI) {
                Player p = getPlayer(id);
                if (p != null && ("ST".equals(p.position) || "CM".equals(p.position))) {
                    captainId = id;
                    break;
                }
            }
            if (captainId == null) captainId = startingXI.get(0);
        }
    }

    public boolean recruit(Player recruit, int coins) {
        if (coins < recruit.price) return false;
        if (getPlayer(recruit.id) != null) return false;
        Player copy = recruit.copy();
        copy.owned = true;
        ownedPlayers.add(copy);
        bench.add(copy.id);
        return true;
    }

    public void swapToStarting(String playerId, String replaceId) {
        if (!startingXI.contains(replaceId)) return;
        if (!bench.contains(playerId) && !startingXI.contains(playerId)) return;
        int idx = startingXI.indexOf(replaceId);
        startingXI.set(idx, playerId);
        bench.remove(playerId);
        if (!bench.contains(replaceId)) bench.add(replaceId);
    }
}
