package com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallEvent
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun FallDetailModal(
    fallEvent: FallEvent,
    emergencyContact: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (fallEvent.wasCancelled)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (fallEvent.wasCancelled) "⚠️ Cancelled Alert" else "🚨 Confirmed Fall",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (fallEvent.wasCancelled)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = fallEvent.timestamp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (fallEvent.wasCancelled)
                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (fallEvent.wasCancelled)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fall Severity Section
                    if (!fallEvent.wasCancelled) {
                        SectionCard(
                            title = "Fall Severity",
                            icon = Icons.Default.Warning
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailRow(
                                    label = "Acceleration Impact",
                                    value = "${"%.2f".format(fallEvent.accelerationMagnitude)} m/s²",
                                    severity = getSeverityLevel(fallEvent.accelerationMagnitude)
                                )

                                DetailRow(
                                    label = "Rotation Impact",
                                    value = "${"%.2f".format(fallEvent.rotationMagnitude)} rad/s",
                                    severity = getRotationSeverity(fallEvent.rotationMagnitude)
                                )

                                // Overall severity indicator
                                Spacer(modifier = Modifier.height(8.dp))
                                SeverityIndicator(
                                    acceleration = fallEvent.accelerationMagnitude,
                                    rotation = fallEvent.rotationMagnitude
                                )
                            }
                        }
                    }

                    // Location Section
                    SectionCard(
                        title = "Location",
                        icon = Icons.Default.LocationOn
                    ) {
                        if (fallEvent.latitude != null && fallEvent.longitude != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Location coordinates
                                Text(
                                    text = fallEvent.locationString ?: "Location available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                // Embedded Google Map
                                val fallLocation =
                                    remember { LatLng(fallEvent.latitude, fallEvent.longitude) }
                                val cameraPositionState = rememberCameraPositionState {
                                    position = CameraPosition.fromLatLngZoom(fallLocation, 15f)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    GoogleMap(
                                        modifier = Modifier.fillMaxSize(),
                                        cameraPositionState = cameraPositionState,
                                        properties = MapProperties(
                                            mapType = MapType.NORMAL,
                                            isMyLocationEnabled = false
                                        ),
                                        uiSettings = MapUiSettings(
                                            zoomControlsEnabled = true,
                                            zoomGesturesEnabled = true,
                                            scrollGesturesEnabled = true,
                                            tiltGesturesEnabled = false,
                                            rotationGesturesEnabled = false,
                                            compassEnabled = false
                                        )
                                    ) {
                                        Marker(
                                            state = MarkerState(position = fallLocation),
                                            title = "Fall Location",
                                            snippet = fallEvent.timestamp
                                        )
                                    }
                                }

                                // Open in Maps button
                                OutlinedButton(
                                    onClick = {
                                        val mapIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://maps.google.com/?q=${fallEvent.latitude},${fallEvent.longitude}")
                                        )
                                        context.startActivity(mapIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open in Google Maps")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Location unavailable",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Emergency Response Section
                    SectionCard(
                        title = "Emergency Response",
                        icon = Icons.Default.Phone
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!fallEvent.wasCancelled) {
                                // Contact notified
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Emergency Contact Notified",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = emergencyContact ?: "No contact set",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }
                                }

                                // SMS sent indicator
                                DetailInfoRow(
                                    icon = Icons.Default.Message,
                                    label = "SMS Alert",
                                    value = "Sent",
                                    valueColor = Color(0xFF4CAF50)
                                )

                                // Call option
                                if (emergencyContact != null && emergencyContact != "Not set") {
                                    OutlinedButton(
                                        onClick = {
                                            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:$emergencyContact")
                                            }
                                            context.startActivity(callIntent)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Call $emergencyContact")
                                    }
                                }
                            } else {
                                // Fall was cancelled
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.tertiaryContainer,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Alert Cancelled",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "No emergency contacts were notified",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Additional Info
                    SectionCard(
                        title = "Event Details",
                        icon = Icons.Default.Info
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailInfoRow(
                                icon = Icons.Default.Schedule,
                                label = "Time",
                                value = fallEvent.timestamp
                            )
                            DetailInfoRow(
                                icon = if (fallEvent.wasCancelled) Icons.Default.Cancel else Icons.Default.Warning,
                                label = "Status",
                                value = if (fallEvent.wasCancelled) "Cancelled" else "Confirmed",
                                valueColor = if (fallEvent.wasCancelled)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Close button
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    severity: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = getSeverityColor(severity)
        ) {
            Text(
                text = severity,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun SeverityIndicator(
    acceleration: Double,
    rotation: Double
) {
    val overallSeverity = when {
        acceleration > 35.0 || rotation > 6.0 -> "Critical"
        acceleration > 28.0 || rotation > 4.0 -> "High"
        acceleration > 25.0 || rotation > 3.0 -> "Moderate"
        else -> "Low"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                getSeverityColor(overallSeverity).copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .border(
                2.dp,
                getSeverityColor(overallSeverity),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = getSeverityColor(overallSeverity),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Overall Severity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = overallSeverity.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = getSeverityColor(overallSeverity)
            )
        }
    }
}

private fun getSeverityLevel(acceleration: Double): String {
    return when {
        acceleration > 35.0 -> "Critical"
        acceleration > 28.0 -> "High"
        acceleration > 25.0 -> "Moderate"
        else -> "Low"
    }
}

private fun getRotationSeverity(rotation: Double): String {
    return when {
        rotation > 6.0 -> "Critical"
        rotation > 4.0 -> "High"
        rotation > 3.0 -> "Moderate"
        else -> "Low"
    }
}

private fun getSeverityColor(severity: String): Color {
    return when (severity) {
        "Critical" -> Color(0xFFD32F2F)
        "High" -> Color(0xFFF57C00)
        "Moderate" -> Color(0xFFFBC02D)
        "Low" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

