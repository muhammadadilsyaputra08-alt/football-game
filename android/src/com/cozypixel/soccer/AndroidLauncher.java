package com.cozypixel.soccer;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Android entry point — landscape, immersive.
 * Jika inisialisasi gagal (mis. native .so tidak ter-package), error ditampilkan
 * di layar alih-alih force close.
 */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useImmersiveMode = true;
            config.useAccelerometer = false;
            config.useCompass = false;
            initialize(new PixelSoccerGame(), config);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            TextView tv = new TextView(this);
            tv.setText("PIXEL SOCCER - STARTUP ERROR\n\n" + sw);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.rgb(64, 13, 20));
            tv.setTextSize(11f);
            tv.setPadding(16, 16, 16, 16);
            ScrollView sv = new ScrollView(this);
            sv.addView(tv);
            setContentView(sv);
        }
    }
}
