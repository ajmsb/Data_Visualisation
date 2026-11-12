package com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils

/**
 * Utility to monitor device battery level
 * Provides real battery percentage for dashboard status
 */

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberBatteryLevel(): State<Int> {
    val context = LocalContext.current

    return produceState(initialValue = 100) {
        val batteryLevel = getBatteryLevel(context)
        value = batteryLevel
    }
}

private fun getBatteryLevel(context: Context): Int {
    val batteryStatus = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    return batteryStatus?.let { intent ->
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100 // Default fallback
        }
    } ?: 100 // Default fallback
}