package com.cozypixel.soccer.engine;

import java.util.ArrayList;
import java.util.List;

/** Ringkasan akhir pertandingan. */
public final class MatchResult {
    public static final class Goal {
        public final int team; public final String name; public final int minute; public final boolean ownGoal;
        public Goal(int team, String name, int minute, boolean og) {
            this.team = team; this.name = name; this.minute = minute; this.ownGoal = og;
        }
    }

    public String homeName, awayName;
    public int homeGoals, awayGoals;
    public int penHome = -1, penAway = -1;     // -1 = tidak ada adu penalti
    public boolean forfeit;
    public boolean extraTime;
    public final List<Goal> goals = new ArrayList<>();
    public final MatchEngine.TeamStats[] stats = {new MatchEngine.TeamStats(), new MatchEngine.TeamStats()};
    public int coinsEarned;
    public String rewardNote = "";

    /** 0 = tuan rumah (pemain) menang, 1 = lawan menang, -1 = seri. */
    public int winner() {
        if (homeGoals != awayGoals) return homeGoals > awayGoals ? 0 : 1;
        if (penHome >= 0 && penHome != penAway) return penHome > penAway ? 0 : 1;
        return -1;
    }
}
