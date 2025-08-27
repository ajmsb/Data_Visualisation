package com.example.practicaltrainingproject.presentation.ui.profileScreen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val savedThreshold = prefs.getFloat("sensitivity_threshold", 5f)
    var threshold by remember { mutableStateOf(savedThreshold) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Sensitivity Threshold", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = threshold,
            onValueChange = {
                threshold = it
                prefs.edit().putFloat("sensitivity_threshold", it).apply()
            },
            valueRange = 1f..10f,
            steps = 8
        )
        Text(text = "Current: ${"%.1f".format(threshold)}")
    }
}
