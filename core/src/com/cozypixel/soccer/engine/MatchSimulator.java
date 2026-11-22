package com.cozypixel.soccer.engine;

import com.cozypixel.soccer.model.Player;

/**
 * Simulasi headless (tanpa grafis): AI vs AI untuk uji logika & balancing.
 * Jalankan:  gradle :core:simulate   atau   java -cp core/build/classes/java/main:gdx.jar ...MatchSimulator [jumlah] [strengthA] [strengthB]
 */
public final class MatchSimulator {
    private MatchSimulator() {}

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 20;
        float sa = args.length > 1 ? Float.parseFloat(args[1]) : 65f;
        float sb = args.length > 2 ? Float.parseFloat(args[2]) : 65f;
        String fb = args.length > 3 ? args[3] : null;
        long t0 = System.nanoTime();
        int[] wins = new int[3];
        final int[] evc = new int[MatchEngine.Ev.values().length];
        double goals = 0, shots = 0, onT = 0, poss0 = 0, passes = 0, tackles = 0, saves = 0;
        for (int i = 0; i < n; i++) {
            MatchConfig cfg = new MatchConfig();
            cfg.home = OpponentFactory.create("A", sa, "4-3-3", 1, 100 + i);
            cfg.home.away = false;
            cfg.away = OpponentFactory.create("B", sb, fb != null ? fb : (i % 2 == 0 ? "4-4-2" : "4-3-3"), 2, 200 + i);
            cfg.matchSeconds = 120f;
            cfg.allowDraw = true;
            cfg.seed = 1000 + i;
            MatchEngine e = new MatchEngine(cfg);
            e.autoplay = true;
            e.setListener((ev, p, x, y, t) -> evc[ev.ordinal()]++);
            int guard = 0;
            while (e.phase != MatchEngine.Phase.FINISHED && guard++ < 60 * 400) {
                e.update(1f / 60f);
            }
            MatchResult r = e.result;
            goals += r.homeGoals + r.awayGoals;
            wins[r.winner() == 0 ? 0 : r.winner() == 1 ? 1 : 2]++;
            for (int k = 0; k < 2; k++) {
                shots += r.stats[k].shots; onT += r.stats[k].onTarget; passes += r.stats[k].passes;
                tackles += r.stats[k].tackles; saves += r.stats[k].saves;
            }
            poss0 += r.stats[0].possession / Math.max(1f, r.stats[0].possession + r.stats[1].possession);
            if (n <= 5) System.out.println("Match " + i + ": " + r.homeGoals + "-" + r.awayGoals
                    + "  shots " + r.stats[0].shots + "/" + r.stats[1].shots);
        }
        StringBuilder sb2 = new StringBuilder();
        for (MatchEngine.Ev ev : MatchEngine.Ev.values()) sb2.append(ev).append('=').append(evc[ev.ordinal()] / (float) n).append(' ');
        System.out.println("events/match: " + sb2);
        double ms = (System.nanoTime() - t0) / 1e6;
        System.out.printf("Matches=%d  W/L/D (A)=%d/%d/%d  goals/match=%.2f  shots/match=%.1f  onTarget=%.1f  passes=%.1f  tackles=%.1f  saves=%.1f  possA=%.0f%%  (%.0f ms)%n",
                n, wins[0], wins[1], wins[2], goals / n, shots / n, onT / n, passes / n, tackles / n, saves / n, poss0 / n * 100, ms);
    }
}
