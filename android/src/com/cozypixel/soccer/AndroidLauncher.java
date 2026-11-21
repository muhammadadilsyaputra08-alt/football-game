package com.cozypixel.soccer;

import android.content.Intent;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Android entry point — landscape, immersive.
 * Jika initialize() gagal, error asli dikirim ke CrashActivity lalu Activity di-finish()
 * (kalau tidak, onResume() -> NPE menutupi error asli).
 */
public class AndroidLauncher extends AndroidApplication {
    private boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useImmersiveMode = true;
            config.useAccelerometer = false;
            config.useCompass = false;
            initialize(new PixelSoccerGame(), config);
            ready = true;
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            Intent i = new Intent(this, CrashActivity.class);
            i.putExtra(CrashActivity.EXTRA_REPORT,
                    "STARTUP (initialize) FAILED\nABI " + android.os.Build.SUPPORTED_ABIS[0]
                            + "\n\n" + sw);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    // Setelah finish() di onCreate, Android tidak memanggil onResume; guard ini hanya jaring pengaman.
    @Override protected void onResume() {
        if (ready) super.onResume();
    }
    @Override protected void onPause() {
        if (ready) super.onPause();
    }
    @Override protected void onDestroy() {
        try { super.onDestroy(); } catch (Throwable ignored) { }
    }
}
