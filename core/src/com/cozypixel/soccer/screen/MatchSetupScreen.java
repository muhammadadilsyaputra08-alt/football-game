package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.engine.Formations;
import com.cozypixel.soccer.engine.MatchConfig;
import com.cozypixel.soccer.engine.MatchEngine;
import com.cozypixel.soccer.engine.MatchResult;
import com.cozypixel.soccer.engine.OpponentFactory;

import java.util.Random;

/** Laga persahabatan: pilih tingkat kesulitan & durasi, lawan acak. */
public class MatchSetupScreen extends BaseScreen {
    private static final String[] LEN_LABEL = {"Singkat (2x45 dtk)", "Normal (2x60 dtk)", "Panjang (2x90 dtk)"};
    private static final float[] LEN_SEC = {90f, 120f, 180f};
    private MatchEngine.Difficulty diff = MatchEngine.Difficulty.NORMAL;
    private int len = 1;
    private final Random rnd = new Random();
    private String oppName = OpponentFactory.CLUB_NAMES[0];
    private int oppEmblem = 2;
    private String oppFormation = "4-3-3";

    public MatchSetupScreen(PixelSoccerGame game) {
        super(game, new Color(0.15f, 0.3f, 0.24f, 1f));
        frame("LAGA PERSAHABATAN");
        reroll();
        refresh();
    }

    private void reroll() {
        oppName = OpponentFactory.CLUB_NAMES[rnd.nextInt(OpponentFactory.CLUB_NAMES.length)];
        oppEmblem = 2 + rnd.nextInt(5);
        oppFormation = Formations.NAMES[rnd.nextInt(Formations.NAMES.length)];
    }

    private float oppStrength() {
        float base = MatchLauncher.ovr(game);
        return base + (diff == MatchEngine.Difficulty.EASY ? -8f : diff == MatchEngine.Difficulty.HARD ? 8f : 0f);
    }

    private void refresh() {
        infoLabel.setText(String.format("Tim kamu OVR %.0f   |   Lawan: %s (%s) OVR ~%.0f",
                MatchLauncher.ovr(game), oppName, oppFormation, oppStrength()));
        content.clear();
        Table d = new Table();
        for (final MatchEngine.Difficulty df : MatchEngine.Difficulty.values()) {
            d.add(button((df == diff ? "* " : "") + df.label, new Runnable() {
                @Override public void run() { diff = df; refresh(); }
            })).width(130).height(34).pad(3);
        }
        content.add(d).row();
        Table l = new Table();
        for (int i = 0; i < LEN_LABEL.length; i++) {
            final int k = i;
            l.add(button((i == len ? "* " : "") + LEN_LABEL[i], new Runnable() {
                @Override public void run() { len = k; refresh(); }
            })).width(190).height(34).pad(3);
        }
        content.add(l).row();
        content.add(button("Ganti Lawan", new Runnable() {
            @Override public void run() { reroll(); refresh(); }
        })).width(200).height(32).row();
        content.add(button("MULAI PERTANDINGAN", new Runnable() {
            @Override public void run() { start(); }
        })).width(300).height(46).padTop(8).row();
        say("Hadiah: menang 100, seri 40, kalah 25 koin + bonus gol & clean sheet");
    }

    private void start() {
        MatchConfig cfg = new MatchConfig();
        cfg.title = "Laga Persahabatan";
        cfg.home = MatchLauncher.homeSpec(game);
        cfg.away = OpponentFactory.create(oppName, oppStrength(), oppFormation, oppEmblem, rnd.nextLong());
        cfg.matchSeconds = LEN_SEC[len];
        cfg.difficulty = diff;
        cfg.allowDraw = true;
        cfg.rewards = true;
        go(new MatchScreen(game, cfg, new MatchScreen.Next() {
            @Override public Screen next(PixelSoccerGame g, MatchResult r) { return new MainMenuScreen(g); }
        }));
    }
}
