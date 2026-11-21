package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cozypixel.soccer.PixelSoccerGame;

/**
 * Settings — Sound / Music / Vibration (tersimpan di save) + reset data (2x tekan).
 * Catatan: audio belum ada di alpha; pengaturan sudah disimpan untuk build berikutnya.
 */
public class SettingsScreen extends BaseScreen {
    private boolean confirmReset = false;

    public SettingsScreen(PixelSoccerGame game) {
        super(game, new Color(0.25f, 0.22f, 0.2f, 1f));
        frame("SETTINGS");
        refresh();
    }

    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

    private void refresh() {
        infoLabel.setText("Pixel Soccer: Cozy Football  v0.1.0-alpha");
        content.clear();

        content.add(button("Sound: " + onOff(game.saveData.soundOn), new Runnable() {
            @Override public void run() {
                game.saveData.soundOn = !game.saveData.soundOn;
                game.saveGame();
                refresh();
                say("Sound " + onOff(game.saveData.soundOn));
            }
        })).width(300).height(38).row();

        content.add(button("Music: " + onOff(game.saveData.musicOn), new Runnable() {
            @Override public void run() {
                game.saveData.musicOn = !game.saveData.musicOn;
                game.saveGame();
                refresh();
                say("Music " + onOff(game.saveData.musicOn));
            }
        })).width(300).height(38).row();

        content.add(button("Vibration: " + onOff(game.saveData.vibrationOn), new Runnable() {
            @Override public void run() {
                game.saveData.vibrationOn = !game.saveData.vibrationOn;
                game.saveGame();
                refresh();
                say("Vibration " + onOff(game.saveData.vibrationOn));
                if (game.saveData.vibrationOn) {
                    try { Gdx.input.vibrate(60); } catch (Throwable ignored) { }
                }
            }
        })).width(300).height(38).row();

        content.add(new Label("(audio belum tersedia di alpha)", skin, "dim")).row();

        content.add(button(confirmReset ? "YAKIN? Tekan lagi untuk HAPUS" : "Reset Data Game", new Runnable() {
            @Override public void run() {
                if (!confirmReset) {
                    confirmReset = true;
                    refresh();
                    say("Semua progress akan dihapus. Tekan sekali lagi untuk konfirmasi.");
                } else {
                    game.resetGame();
                    go(new MainMenuScreen(game));
                }
            }
        })).width(300).height(38).padTop(14).row();
    }
}
