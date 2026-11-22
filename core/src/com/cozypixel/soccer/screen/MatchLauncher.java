package com.cozypixel.soccer.screen;

import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.engine.TeamSpec;
import com.cozypixel.soccer.model.Player;
import com.cozypixel.soccer.model.Team;

import java.util.ArrayList;
import java.util.List;

/** Util pembentuk TeamSpec pemain dari data save. */
final class MatchLauncher {
    private MatchLauncher() {}

    static TeamSpec homeSpec(PixelSoccerGame game) {
        Team team = game.playerTeam;
        TeamSpec t = new TeamSpec();
        t.name = game.saveData.club.name;
        t.formation = team.formation;
        t.captainId = team.captainId;
        t.emblem = 1;
        t.xi.addAll(team.getStartingPlayers());
        if (t.xi.size() < 11) { // lengkapi dari pemain lain bila XI kurang
            List<Player> rest = new ArrayList<>(team.ownedPlayers);
            rest.removeAll(t.xi);
            for (Player p : rest) { if (t.xi.size() >= 11) break; t.xi.add(p); }
        }
        return t;
    }

    static float ovr(PixelSoccerGame game) { return homeSpec(game).ovr(); }
}
