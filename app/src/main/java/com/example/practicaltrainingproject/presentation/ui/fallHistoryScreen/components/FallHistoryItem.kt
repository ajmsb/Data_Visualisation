package com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components

/**
 * Individual fall history item card showing event details
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
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
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallEvent

@Composable
fun FallHistoryItem(
    event: FallEvent,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (event.wasCancelled)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (event.wasCancelled) Icons.Default.Cancel else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (event.wasCancelled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (event.wasCancelled) "Cancelled Alert" else "Fall Detected",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (event.wasCancelled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = event.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!event.wasCancelled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Impact: ${"%.1f".format(event.accelerationMagnitude)} m/s²",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Rotation: ${"%.1f".format(event.rotationMagnitude)} rad/s",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = if (event.accelerationMagnitude > 25) "High Impact" else "Medium Impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (event.accelerationMagnitude > 25) MaterialTheme.colorScheme.error else Color(
                            0xFFFFA726
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}