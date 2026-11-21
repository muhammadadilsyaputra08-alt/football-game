package com.cozypixel.soccer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Desktop launcher untuk testing cepat.
 * Resolusi 1280x768 (2x virtual 640x384) agar pixel art tajam.
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Pixel Soccer: Cozy Football — Alpha");
        config.setWindowedMode(1280, 768);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setResizable(true);
        config.setWindowIcon(
            "CozyPixelSoccer/Branding/App_Icon_128.png",
            "CozyPixelSoccer/Branding/App_Icon_64.png",
            "CozyPixelSoccer/Branding/App_Icon_32.png");
        new Lwjgl3Application(new PixelSoccerGame(), config);
    }
}
