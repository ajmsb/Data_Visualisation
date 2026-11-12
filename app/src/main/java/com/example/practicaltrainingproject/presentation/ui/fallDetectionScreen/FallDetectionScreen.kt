package com.example.practicaltrainingproject.presentation.ui.fallDetectionScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallDetectionViewModel
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallEvent

@Composable
fun FallDetectionScreen(
    fallDetectionViewModel: FallDetectionViewModel
) {
    val uiState by fallDetectionViewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Show loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Initializing sensors...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        // Show error state
        if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Sensor Error",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "Unknown sensor error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(onClick = { fallDetectionViewModel.toggleMonitoring() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Fall Detection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Monitoring Toggle
            MonitoringCard(
                isMonitoring = uiState.isMonitoring,
                sensorAccuracy = uiState.sensorAccuracy,
                onToggle = { fallDetectionViewModel.toggleMonitoring() },
                onSettingsClick = { showSettingsDialog = true }
            )

            // Fall Alert with Countdown
            if (uiState.fallDetected && uiState.countdownSeconds > 0) {
                FallCountdownCard(
                    countdown = uiState.countdownSeconds,
                    onCancel = { fallDetectionViewModel.cancelFallAlert() }
                )
            }

            // Confirmed Fall Alert
            if (uiState.fallDetected && uiState.countdownSeconds == 0) {
                FallAlertCard(onDismiss = { fallDetectionViewModel.dismissFallAlert() })
            }

            // Sensor Data
            if (uiState.isMonitoring) {
                SensorDataCard(
                    accel = uiState.accelerometerData,
                    gyro = uiState.gyroscopeData,
                    fallDetected = uiState.fallDetected,
                    accelThreshold = uiState.accelerationThreshold,
                    gyroThreshold = uiState.rotationThreshold
                )
            }

            // Emergency Button
            EmergencyButton(onClick = { fallDetectionViewModel.manualEmergency() })

            // Statistics
            StatisticsCard(
                fallCount = uiState.fallCount,
                isMonitoring = uiState.isMonitoring,
                lastFallTimestamp = uiState.lastFallTimestamp,
                onViewHistory = { showHistoryDialog = true },
                onClearHistory = { fallDetectionViewModel.clearHistory() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            accelThreshold = uiState.accelerationThreshold,
            gyroThreshold = uiState.rotationThreshold,
            onAccelThresholdChange = { fallDetectionViewModel.updateAccelerationThreshold(it) },
            onGyroThresholdChange = { fallDetectionViewModel.updateRotationThreshold(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // History Dialog
    if (showHistoryDialog) {
        FallHistoryDialog(
            history = uiState.fallHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
fun MonitoringCard(
    isMonitoring: Boolean,
    sensorAccuracy: String,
    onToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
                        .background(
                            if (isMonitoring) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isMonitoring) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Monitoring Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Toggle and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMonitoring) "🟢 Active" else "🔴 Inactive",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isMonitoring) "Monitoring movements" else "Tap to start monitoring",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isMonitoring, onCheckedChange = { onToggle() })
            }

            if (isMonitoring) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sensor Accuracy: $sensorAccuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FallCountdownCard(countdown: Int, onCancel: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Fall detected",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "⚠️ FALL DETECTED!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Are you okay?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { countdown / 10f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "$countdown",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Emergency services will be alerted in $countdown seconds",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I'M OK - CANCEL ALERT", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FallAlertCard(onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Fall confirmed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "🚨 EMERGENCY ALERT SENT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Emergency contacts notified",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISMISS")
            }
        }
    }
}

@Composable
fun SensorDataCard(
    accel: Triple<Double, Double, Double>,
    gyro: Triple<Double, Double, Double>,
    fallDetected: Boolean,
    accelThreshold: Double,
    gyroThreshold: Double
) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Live Sensor Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (fallDetected) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (fallDetected) "ALERT" else "NORMAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (fallDetected) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Accelerometer Data
            Column {
                Text(
                    "📱 Accelerometer (m/s²)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "X: ${"%.2f".format(accel.first)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Y: ${"%.2f".format(accel.second)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Z: ${"%.2f".format(accel.third)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                val accelMagnitude = kotlin.math.sqrt(
                    accel.first * accel.first +
                            accel.second * accel.second +
                            accel.third * accel.third
                )
                val netAccel = kotlin.math.abs(accelMagnitude - 9.8)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Magnitude: ${"%.2f".format(netAccel)} m/s² (threshold: ${
                        "%.1f".format(
                            accelThreshold
                        )
                    })",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (netAccel > accelThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Gyroscope Data
            Column {
                Text(
                    "🔄 Gyroscope (rad/s)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${"%.2f".format(gyro.first)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Y: ${"%.2f".format(gyro.second)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Z: ${"%.2f".format(gyro.third)}", style = MaterialTheme.typography.bodyMedium)

                val gyroMagnitude = kotlin.math.sqrt(
                    gyro.first * gyro.first +
                            gyro.second * gyro.second +
                            gyro.third * gyro.third
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Magnitude: ${"%.2f".format(gyroMagnitude)} rad/s (threshold: ${
                        "%.1f".format(
                            gyroThreshold
                        )
                    })",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (gyroMagnitude > gyroThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "🚨 EMERGENCY SOS",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun StatisticsCard(
    fallCount: Int,
    isMonitoring: Boolean,
    lastFallTimestamp: String,
    onViewHistory: () -> Unit,
    onClearHistory: () -> Unit
) {
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
            // Section Header with Icon Badge and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row {
                    IconButton(onClick = onViewHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "View History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onClearHistory, enabled = fallCount > 0) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = if (fallCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Statistics Content
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total falls detected:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$fallCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Monitoring status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isMonitoring) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isMonitoring) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Last fall:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        lastFallTimestamp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val safetyScore = (100 - (fallCount * 10).coerceAtMost(100))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Safety score:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$safetyScore%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            safetyScore >= 80 -> Color(0xFF4CAF50)
                            safetyScore >= 50 -> Color(0xFFFFA726)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    accelThreshold: Double,
    gyroThreshold: Double,
    onAccelThresholdChange: (Double) -> Unit,
    onGyroThresholdChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fall Detection Settings") },
        text = {
            Column {
                Text(
                    "Acceleration Threshold: ${"%.1f".format(accelThreshold)} m/s²",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = accelThreshold.toFloat(),
                    onValueChange = { onAccelThresholdChange(it.toDouble()) },
                    valueRange = 10f..50f,
                    steps = 39
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Rotation Threshold: ${"%.1f".format(gyroThreshold)} rad/s",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = gyroThreshold.toFloat(),
                    onValueChange = { onGyroThresholdChange(it.toDouble()) },
                    valueRange = 1f..10f,
                    steps = 89
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Lower values = more sensitive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun FallHistoryDialog(
    history: List<FallEvent>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fall History")
            }
        },
        text = {
            if (history.isEmpty()) {
                Text("No fall events recorded")
            } else {
                LazyColumn {
                    items(history.reversed().size) { index ->
                        val event = history.reversed()[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (event.wasCancelled)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    if (event.wasCancelled) {
                                        "⚠️ Cancelled Alert"
                                    } else {
                                        "🚨 Fall Detected"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    event.timestamp,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (!event.wasCancelled) {
                                    Text(
                                        "Accel: ${"%.1f".format(event.accelerationMagnitude)} m/s²",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Rotation: ${"%.1f".format(event.rotationMagnitude)} rad/s",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
