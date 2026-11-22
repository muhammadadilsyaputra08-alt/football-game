package com.cozypixel.soccer.engine;

import com.cozypixel.soccer.model.Player;

import java.util.Random;

/** Pembuat tim lawan (AI) dengan kekuatan tertentu. */
public final class OpponentFactory {
    private OpponentFactory() {}

    public static final String[] CLUB_NAMES = {
            "Warung United", "Kopi Rovers", "Soto Athletic", "Teh Hangat FC", "Pixel City",
            "Goal Getters", "Senja Wanderers", "Batik Stars", "Angkringan FC", "Sawah Hijau",
            "Mie Rebus SC", "Langit Biru"
    };
    private static final String[] FIRST = {"Adi", "Bayu", "Cahyo", "Doni", "Eko", "Farhan", "Gilang", "Hadi",
            "Indra", "Joko", "Kevin", "Lutfi", "Mario", "Naufal", "Oki", "Putra", "Rizky", "Sandi",
            "Taufik", "Umar", "Vino", "Wahyu", "Yusuf", "Zaki"};

    /** strength ~ 40..90 = rata-rata stat inti. */
    public static TeamSpec create(String clubName, float strength, String formation, int emblem, long seed) {
        Random r = new Random(seed);
        TeamSpec t = new TeamSpec();
        t.name = clubName;
        t.formation = formation;
        t.emblem = emblem;
        t.away = true;
        java.util.List<Formations.Slot> slots = Formations.get(formation);
        for (int i = 0; i < slots.size(); i++) {
            String role = slots.get(i).role;
            Player p = new Player("opp" + i, FIRST[r.nextInt(FIRST.length)], role);
            p.number = i == 0 ? 1 : i + 1;
            float s = strength + (r.nextFloat() - 0.5f) * 8f;
            if (role.equals("GK")) {
                p.setStat("Reflex", s + 2); p.setStat("Handling", s); p.setStat("Positioning", s);
                p.setStat("Diving", s); p.setStat("Distribution", s - 6); p.setStat("Physical", s - 2);
            } else {
                boolean def = role.equals("CB") || role.equals("RB") || role.equals("LB") || role.equals("CDM");
                boolean att = role.equals("ST") || role.equals("RW") || role.equals("LW") || role.equals("CAM");
                p.setStat("Pace", s + (role.endsWith("W") || role.endsWith("B") ? 6 : 0));
                p.setStat("Shooting", s + (att ? 8 : -14));
                p.setStat("Passing", s + (role.startsWith("C") ? 5 : -3));
                p.setStat("Dribbling", s + (att ? 5 : -8));
                p.setStat("Defense", s + (def ? 10 : -16));
                p.setStat("Physical", s + (role.equals("CB") ? 8 : -2));
            }
            t.xi.add(p);
        }
        return t;
    }
}
