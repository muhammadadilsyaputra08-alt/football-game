package com.cozypixel.soccer.engine;

/** Parameter satu pertandingan (murni data, tanpa ketergantungan UI). */
public final class MatchConfig {
    public String title = "Laga Persahabatan";
    public TeamSpec home;
    public TeamSpec away;
    public float matchSeconds = 120f;      // total detik nyata (2 babak)
    public boolean allowDraw = true;       // false = perpanjangan golden goal + adu penalti
    public MatchEngine.Difficulty difficulty = MatchEngine.Difficulty.NORMAL;
    public boolean rewards = true;         // beri koin & statistik
    public int rewardWin = 100, rewardDraw = 40, rewardLose = 25;
    public long seed = System.nanoTime();
}
