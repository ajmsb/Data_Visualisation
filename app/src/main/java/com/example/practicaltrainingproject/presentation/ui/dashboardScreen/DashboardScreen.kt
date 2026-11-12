package com.example.practicaltrainingproject.presentation.ui.dashboardScreen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.DeviceStatusCard
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.EmergencyReadyCard
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.HeroStatsCard
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.QuickActionsGrid
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.RecentActivityCard
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.SafetyOverviewCard
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.StatusBadges
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils.ActivityTracker
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils.SafetyScoreCalculator
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils.rememberBatteryLevel
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallDetectionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    fallDetectionViewModel: FallDetectionViewModel
) {
    val fallState by fallDetectionViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val batteryLevel by rememberBatteryLevel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize activity tracker
    val activityTracker = remember { ActivityTracker(context) }
    val activeDays by activityTracker.activeDays.collectAsState(initial = 0)
    val hasRecentActivity = activityTracker.hasRecentActivity(fallState.lastFallTimestamp)

    // Record activity when monitoring is active
    LaunchedEffect(fallState.isMonitoring) {
        if (fallState.isMonitoring) {
            activityTracker.recordDailyActivity()
        }
    }

    // Calculate real safety score
    val safetyScore = SafetyScoreCalculator.calculateSafetyScore(
        fallCount = fallState.fallCount,
        isMonitoring = fallState.isMonitoring,
        activeDays = activeDays,
        recentActivity = hasRecentActivity
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Show loading indicator
        if (fallState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Show error state
        else if (fallState.error != null) {
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
                        Text(
                            text = "Error Loading Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fallState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    activityTracker.recordDailyActivity()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
        // Show content
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modern Header with Greeting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Hello",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.WavingHand,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }


                // Hero Stats Card - Now with real data
                HeroStatsCard(
                    safetyScore = safetyScore,
                    fallCount = fallState.fallCount,
                    activeDays = activeDays
                )

                // Status Badges - Now with real battery and sensor data
                StatusBadges(
                    isMonitoring = fallState.isMonitoring,
                    sensorAccuracy = fallState.sensorAccuracy,
                    batteryLevel = batteryLevel
                )

                // Quick Actions Grid
                QuickActionsGrid(
                    onFallDetection = { navController.navigate("fall_detection") },
                    onProfile = { navController.navigate("profile") },
                    onSettings = { navController.navigate("settings") },
                    onHistory = { navController.navigate("fall_history") }
                )

                // Safety Overview Card
                SafetyOverviewCard(
                    isMonitoring = fallState.isMonitoring,
                    fallCount = fallState.fallCount,
                    lastFallTimestamp = fallState.lastFallTimestamp
                )

                // Device Status Card
                DeviceStatusCard(
                    isMonitoring = fallState.isMonitoring,
                    sensorAccuracy = fallState.sensorAccuracy,
                    batteryLevel = batteryLevel
                )

                // Emergency Ready Card
                EmergencyReadyCard(
                    emergencyContact = currentUser?.emergencyContact,
                    isMonitoring = fallState.isMonitoring
                )

                // Recent Activity Card
                RecentActivityCard(
                    fallCount = fallState.fallCount,
                    lastFallTimestamp = fallState.lastFallTimestamp,
                    onViewHistory = { navController.navigate("fall_history") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
