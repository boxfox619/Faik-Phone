package com.boxfox.faikphone.utils;

import android.os.Vibrator;

/**
 * Created by dsm_025 on 2017-05-30.
 */

public class VibrateManager extends Thread {
    Vibrator vibrator;

    public VibrateManager(Vibrator vibrator){
        this.vibrator = vibrator;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            vibrator.vibrate(1000);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
