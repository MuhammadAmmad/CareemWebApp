package com.careemwebapp.utils;

import android.os.Handler;
import android.os.Looper;


/**
 * Created by alex on 21.10.15.
 */
public final class ActivityUtil {
    private static void scheduleOnMainThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static void scheduleOnMainThread(Runnable r, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(r, delay);
    }

    public static void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            scheduleOnMainThread(r);
        }
    }

}
