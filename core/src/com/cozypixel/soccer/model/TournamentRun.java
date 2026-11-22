package com.cozypixel.soccer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Status satu turnamen knockout yang sedang dimainkan (disimpan di memori selama sesi). */
public final class TournamentRun {
    public final int cupIdx;
    public final String cupName;
    public final String[] teamNames;      // indeks 0 = klub pemain
    public final float[] strength;        // kekuatan tim AI (indeks 0 diabaikan)
    public final int[] emblem;
    public final List<Integer> alive = new ArrayList<>();
    public final List<String> log = new ArrayList<>();
    public int round;
    public boolean eliminated, champion;
    public String finishedLabel;          // "Juara", "Runner-up", "Semifinal", ...

    public TournamentRun(int cupIdx, String cupName, String[] names, float[] strength, int[] emblem) {
        this.cupIdx = cupIdx; this.cupName = cupName; this.teamNames = names;
        this.strength = strength; this.emblem = emblem;
        for (int i = 0; i < names.length; i++) alive.add(i);
    }

    public String roundName() {
        switch (alive.size()) {
            case 2: return "Final";
            case 4: return "Semifinal";
            case 8: return "Perempat Final";
            default: return "Babak " + (round + 1);
        }
    }

    public int opponent() { return alive.get(1); }

    /** Catat hasil laga pemain, simulasikan laga lain, bentuk babak berikut. */
    public void advance(boolean playerWon, Random r) {
        String rn = roundName();
        int opp = opponent();
        log.add(rn + ": " + teamNames[0] + (playerWon ? " menang atas " : " kalah dari ") + teamNames[opp]);
        List<Integer> nextAlive = new ArrayList<>();
        if (playerWon) nextAlive.add(0);
        for (int i = 2; i + 1 < alive.size(); i += 2) {
            int a = alive.get(i), b = alive.get(i + 1);
            float p = Math.max(0.15f, Math.min(0.85f, 0.5f + (strength[a] - strength[b]) / 60f));
            int w = r.nextFloat() < p ? a : b;
            nextAlive.add(w);
        }
        if (!playerWon) {
            eliminated = true;
            finishedLabel = rn.equals("Final") ? "Runner-up" : rn;
            return;
        }
        if (alive.size() == 2) { champion = true; finishedLabel = "Juara"; return; }
        alive.clear();
        alive.addAll(nextAlive);
        round++;
    }
}
