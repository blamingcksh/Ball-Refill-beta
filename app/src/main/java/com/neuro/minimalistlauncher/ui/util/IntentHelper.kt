package com.neuro.minimalistlauncher.ui.util

import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast

object IntentHelper {

    fun launchApp(context: Context, packageName: String, activityName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(packageName, activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // Fallback to PackageManager launch intent
            try {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallbackIntent != null) {
                    context.startActivity(fallbackIntent)
                    true
                } else {
                    Toast.makeText(context, "Cannot open app", Toast.LENGTH_SHORT).show()
                    false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open app", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun openClock(context: Context) {
        val intents = listOf(
            Intent(AlarmClock.ACTION_SHOW_ALARMS),
            Intent(AlarmClock.ACTION_SET_ALARM)
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) {}
        }

        // Package lookup fallback for common manufacturer clock apps
        val clockPackages = listOf(
            "com.google.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.android.deskclock",
            "com.oneplus.deskclock",
            "com.coloros.alarmclock",
            "com.miui.deskclock"
        )
        for (pkg in clockPackages) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                }
            } catch (_: Exception) {}
        }

        Toast.makeText(context, "No clock app found", Toast.LENGTH_SHORT).show()
    }

    fun openCalendar(context: Context) {
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR),
            Intent(Intent.ACTION_VIEW).setData(
                ContentUris.appendId(
                    CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
                    System.currentTimeMillis()
                ).build()
            )
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) {}
        }

        Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
    }

    fun openAppSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Cannot open settings", Toast.LENGTH_SHORT).show()
        }
    }
}
