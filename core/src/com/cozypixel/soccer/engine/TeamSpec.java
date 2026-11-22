package com.cozypixel.soccer.engine;

import com.cozypixel.soccer.model.Player;

import java.util.ArrayList;
import java.util.List;

/** Susunan tim yang masuk ke pertandingan. */
public final class TeamSpec {
    public String name;
    public String formation = "4-3-3";
    public int emblem = 1;                 // Team_Emblem_0N
    public boolean away;                   // pakai sprite *_away
    public String captainId;
    public final List<Player> xi = new ArrayList<>();

    public float ovr() {
        if (xi.isEmpty()) return 0f;
        float s = 0f;
        for (Player p : xi) s += p.getOverall();
        return s / xi.size();
    }
}
