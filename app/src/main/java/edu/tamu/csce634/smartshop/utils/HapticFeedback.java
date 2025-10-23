package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

public class HapticFeedback {
    
    // Light click feedback
    public static void lightClick(View view) {
        if (view != null) {
            view.performHapticFeedback(
                android.view.HapticFeedbackConstants.CLOCK_TICK,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }
    
    // Medium click feedback
    public static void mediumClick(View view) {
        if (view != null) {
            view.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }
    
    // Heavy click feedback (for important actions)
    public static void heavyClick(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }
    
    // Success feedback (for add to cart)
    public static void success(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = {0, 30, 50, 30};
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                long[] pattern = {0, 30, 50, 30};
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}