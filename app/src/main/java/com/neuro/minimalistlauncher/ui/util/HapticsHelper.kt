package com.neuro.minimalistlauncher.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticsHelper {

    /**
     * Performs an ultra-light, subtle haptic tick for explicit user selections.
     * Prevents disruptive vibration patterns and sensory panic.
     */
    fun performLightTick(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                vibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(10, 40)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (_: Exception) {
            // Silently ignore if device doesn't allow vibration
        }
    }
}
