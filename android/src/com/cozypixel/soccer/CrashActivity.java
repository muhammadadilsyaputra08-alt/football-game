package com.cozypixel.soccer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Menampilkan laporan crash (berjalan di proses ":crash"). */
public class CrashActivity extends Activity {
    public static final String EXTRA_REPORT = "report";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        final String report = getIntent().getStringExtra(EXTRA_REPORT);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(64, 13, 20));
        root.setPadding(16, 16, 16, 16);

        Button copy = new Button(this);
        copy.setText("Salin laporan crash");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("crash", report));
            Toast.makeText(this, "Tersalin", Toast.LENGTH_SHORT).show();
        });
        root.addView(copy);

        TextView tv = new TextView(this);
        tv.setText("PIXEL SOCCER - CRASH\n\n" + report);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(11f);
        tv.setTextIsSelectable(true);
        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        root.addView(sv);
        setContentView(root);
    }
}
