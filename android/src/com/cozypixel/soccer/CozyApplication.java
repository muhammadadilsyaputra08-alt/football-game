package com.cozypixel.soccer;

import android.app.Application;
import android.content.Intent;
import android.os.Process;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Menangkap SEMUA exception yang tidak tertangani (termasuk sebelum Activity dibuat)
 * lalu menampilkannya di CrashActivity (proses terpisah) alih-alih force close senyap.
 */
public class CozyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                String report = "Thread: " + thread.getName()
                        + "\nAndroid API " + android.os.Build.VERSION.SDK_INT
                        + " | " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
                        + " | ABI " + android.os.Build.SUPPORTED_ABIS[0] + "\n\n" + sw;
                Intent i = new Intent(this, CrashActivity.class);
                i.putExtra(CrashActivity.EXTRA_REPORT, report);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                Thread.sleep(600);
            } catch (Throwable ignored) {
                if (previous != null) previous.uncaughtException(thread, throwable);
            }
            Process.killProcess(Process.myPid());
            System.exit(10);
        });
    }
}
