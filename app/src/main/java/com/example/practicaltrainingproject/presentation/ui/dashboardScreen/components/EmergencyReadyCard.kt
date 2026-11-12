package com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EmergencyReadyCard(
    emergencyContact: String?,
    isMonitoring: Boolean
) {
    val isContactSet = !emergencyContact.isNullOrBlank()
    val isSystemReady = isContactSet && isMonitoring

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header with Icon Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Emergency Ready",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Contact Status
            EmergencyStatusItem(
                icon = Icons.Default.ContactEmergency,
                label = "Emergency Contact",
                status = if (isContactSet) "Configured" else "Not Set",
                isConfigured = isContactSet,
                description = if (isContactSet) emergencyContact
                    ?: "" else "Add contact in settings"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Monitoring Status
            EmergencyStatusItem(
                icon = Icons.Default.NotificationsActive,
                label = "Monitoring System",
                status = if (isMonitoring) "Active" else "Inactive",
                isConfigured = isMonitoring,
                description = if (isMonitoring) "Ready to detect falls" else "Enable in fall detection"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            Text(
                text = if (isSystemReady)
                    "✅ Your safety system is fully operational and ready to respond to emergencies."
                else
                    "⚠️ Complete setup to ensure full emergency protection.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSystemReady) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmergencyStatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    status: String,
    isConfigured: Boolean,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = if (isConfigured) "Configured" else "Not configured",
            modifier = Modifier.size(20.dp),
            tint = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )
    }
}