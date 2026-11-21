package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cozypixel.soccer.PixelSoccerGame;

/**
 * Menu utama — grid 2 kolom agar semua tombol muat di layar landscape.
 */
public class MainMenuScreen extends BaseScreen {

    public MainMenuScreen(PixelSoccerGame game) {
        super(game, new Color(0.15f, 0.35f, 0.22f, 1f));
        root.top().pad(10);

        String header = game.saveData.club.name + "   |   " + game.saveData.club.coins
                + " koin   |   " + game.playerTeam.ownedPlayers.size() + " pemain";
        root.add(new Label(header, skin, "dim")).colspan(2).padTop(4).row();
        root.add(new Label("PIXEL SOCCER", skin, "title")).colspan(2).padTop(14).row();
        root.add(new Label("Cozy Football", skin)).colspan(2).padBottom(18).row();

        add("Training", new Runnable() { public void run() { go(new TrainingScreen(MainMenuScreen.this.game)); } });
        add("Tournament", new Runnable() { public void run() { go(new TournamentScreen(MainMenuScreen.this.game)); } });
        root.row();
        add("Team", new Runnable() { public void run() { go(new TeamScreen(MainMenuScreen.this.game)); } });
        add("Players", new Runnable() { public void run() { go(new PlayersScreen(MainMenuScreen.this.game)); } });
        root.row();
        add("Shop", new Runnable() { public void run() { go(new ShopScreen(MainMenuScreen.this.game)); } });
        add("Settings", new Runnable() { public void run() { go(new SettingsScreen(MainMenuScreen.this.game)); } });
        root.row();
    }

    private void add(String text, Runnable action) {
        root.add(button(text, action)).width(220).height(42).pad(6);
    }

    @Override
    protected void onBack() {
        Gdx.app.exit(); // tombol Back di menu utama = keluar
    }
}
