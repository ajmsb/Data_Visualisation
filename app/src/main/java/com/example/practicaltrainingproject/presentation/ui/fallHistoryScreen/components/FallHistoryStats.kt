package com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components

/**
 * Statistics card showing overview of fall history data
 * Displays total falls, confirmed falls, and cancelled alerts
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FallHistoryStats(
    totalFalls: Int,
    cancelledAlerts: Int,
    confirmedFalls: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Fall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Events
                StatItem(
                    icon = Icons.Default.Warning,
                    value = totalFalls.toString(),
                    label = "Total Events",
                    iconColor = MaterialTheme.colorScheme.primary
                )

                // Confirmed Falls
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = confirmedFalls.toString(),
                    label = "Confirmed Falls",
                    iconColor = MaterialTheme.colorScheme.error
                )

                // Cancelled Alerts
                StatItem(
                    icon = Icons.Default.Cancel,
                    value = cancelledAlerts.toString(),
                    label = "Cancelled Alerts",
                    iconColor = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Text
            Text(
                text = when {
                    confirmedFalls > 0 -> "⚠️ $confirmedFalls fall${if (confirmedFalls != 1) "s" else ""} required attention"
                    cancelledAlerts > 0 -> "✅ $cancelledAlerts alert${if (cancelledAlerts != 1) "s" else ""} were false alarms"
                    else -> "No confirmed falls detected"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}