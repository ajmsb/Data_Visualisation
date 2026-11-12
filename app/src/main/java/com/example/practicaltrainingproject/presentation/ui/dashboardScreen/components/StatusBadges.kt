package com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadges(
    isMonitoring: Boolean,
    sensorAccuracy: String,
    batteryLevel: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusBadge(
            icon = Icons.Default.Sensors,
            label = if (isMonitoring) "Active" else "Inactive",
            backgroundColor = if (isMonitoring) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            textColor = if (isMonitoring) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
        )

        StatusBadge(
            icon = Icons.Default.SignalCellularAlt,
            label = sensorAccuracy,
            backgroundColor = when (sensorAccuracy) {
                "High" -> MaterialTheme.colorScheme.primaryContainer
                "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                "Low" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            textColor = when (sensorAccuracy) {
                "High" -> MaterialTheme.colorScheme.onPrimaryContainer
                "Medium" -> MaterialTheme.colorScheme.onTertiaryContainer
                "Low" -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        StatusBadge(
            icon = Icons.Default.BatteryFull,
            label = "$batteryLevel%",
            backgroundColor = when {
                batteryLevel > 50 -> MaterialTheme.colorScheme.primaryContainer
                batteryLevel > 20 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            },
            textColor = when {
                batteryLevel > 50 -> MaterialTheme.colorScheme.onPrimaryContainer
                batteryLevel > 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onErrorContainer
            }
        )
    }
}

@Composable
private fun StatusBadge(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}