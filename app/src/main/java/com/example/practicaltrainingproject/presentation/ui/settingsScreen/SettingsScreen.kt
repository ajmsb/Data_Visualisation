package com.example.practicaltrainingproject.presentation.ui.settingsScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.practicaltrainingproject.data.manager.NotificationHelper
import com.example.practicaltrainingproject.data.manager.SettingsManager
import com.example.practicaltrainingproject.data.service.FallDetectionService
import com.example.practicaltrainingproject.data.service.LocationService
import com.example.practicaltrainingproject.presentation.ui.components.ExportUtil
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallDetectionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    fallDetectionViewModel: FallDetectionViewModel,
    navController: NavHostController
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val uiState by fallDetectionViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Create settings manager
    val settingsManager = remember { SettingsManager(context) }

    // Create notification helper
    val notificationHelper = remember { NotificationHelper(context) }

    // Track system notification permission status
    var systemNotificationsEnabled by remember { mutableStateOf(notificationHelper.areNotificationsEnabled()) }

    // Collect persistent settings
    val darkTheme by settingsManager.darkTheme.collectAsState(initial = false)
    val notifications by settingsManager.notificationsEnabled.collectAsState(initial = true)
    val soundAlerts by settingsManager.soundAlerts.collectAsState(initial = true)
    val shareData by settingsManager.shareData.collectAsState(initial = false)
    val autoEmergencyCall by settingsManager.autoEmergencyCall.collectAsState(initial = true)
    val autoEmergencySms by settingsManager.autoEmergencySms.collectAsState(initial = true)
    val highAccuracyMode by settingsManager.highAccuracyMode.collectAsState(initial = false)
    val backgroundMonitoring by settingsManager.backgroundMonitoring.collectAsState(initial = false)
    val locationEnabled by settingsManager.locationEnabled.collectAsState(initial = false)

    // Create notification channels on first launch
    LaunchedEffect(Unit) {
        notificationHelper.createNotificationChannels()
        systemNotificationsEnabled = notificationHelper.areNotificationsEnabled()
    }

    // Initialize service states based on persistent settings
    LaunchedEffect(backgroundMonitoring) {
        if (backgroundMonitoring && !uiState.isMonitoring) {
            val intent = Intent(context, FallDetectionService::class.java).apply {
                action = FallDetectionService.ACTION_START_MONITORING
            }
            context.startForegroundService(intent)
        }
    }

    LaunchedEffect(highAccuracyMode) {
        if (highAccuracyMode) {
            fallDetectionViewModel.updateAccelerationThreshold(20.0)
            fallDetectionViewModel.updateRotationThreshold(2.0)
        } else {
            fallDetectionViewModel.updateAccelerationThreshold(25.0)
            fallDetectionViewModel.updateRotationThreshold(3.0)
        }
    }

    // Start location updates when location is enabled
    LaunchedEffect(locationEnabled) {
        val locationService = LocationService.getInstance(context)
        if (locationEnabled) {
            locationService.startLocationUpdates()
        } else {
            locationService.stopLocationUpdates()
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val granted = fineLocationGranted || coarseLocationGranted

        coroutineScope.launch {
            settingsManager.setLocationEnabled(granted)
        }

        if (granted) {
            // Start location service using singleton
            val locationService = LocationService.getInstance(context)
            locationService.startLocationUpdates()
        }
    }

    // SMS permission launcher for emergency SMS
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(
                context,
                "SMS permission granted. Emergency SMS will be sent automatically.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "SMS permission denied. Only WhatsApp alerts will be available.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Request SMS permission on first launch if not granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Profile Header
            UserProfileHeader(currentUser?.email ?: "User")

            // Safety & Monitoring Section - MOST IMPORTANT
            ModernSection(
                title = "Safety & Monitoring",
                icon = Icons.Default.Security,
                iconColor = MaterialTheme.colorScheme.error
            ) {
                // Background Monitoring
                ModernToggleItem(
                    icon = Icons.Default.Security,
                    title = "Background Monitoring",
                    description = "Detect falls even when app is closed",
                    isEnabled = backgroundMonitoring,
                    iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    onToggle = {
                        coroutineScope.launch {
                            val newState = !backgroundMonitoring
                            settingsManager.setBackgroundMonitoring(newState)

                            val intent = Intent(context, FallDetectionService::class.java).apply {
                                action = if (newState) {
                                    FallDetectionService.ACTION_START_MONITORING
                                } else {
                                    FallDetectionService.ACTION_STOP_MONITORING
                                }
                            }

                            try {
                                if (newState) {
                                    context.startForegroundService(intent)
                                    if (!uiState.isMonitoring) {
                                        fallDetectionViewModel.toggleMonitoring()
                                    }
                                } else {
                                    context.stopService(intent)
                                    if (uiState.isMonitoring) {
                                        fallDetectionViewModel.toggleMonitoring()
                                    }
                                }
                            } catch (_: Exception) {
                                // Revert on failure
                                settingsManager.setBackgroundMonitoring(!newState)
                            }
                        }
                    }
                )

                ModernDivider()

                // High Accuracy Mode
                ModernToggleItem(
                    icon = Icons.Default.Speed,
                    title = "High Accuracy Mode",
                    description = "More sensitive detection (uses more battery)",
                    isEnabled = highAccuracyMode,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onToggle = {
                        coroutineScope.launch {
                            val newState = !highAccuracyMode
                            settingsManager.setHighAccuracyMode(newState)

                            if (newState) {
                                fallDetectionViewModel.updateAccelerationThreshold(20.0)
                                fallDetectionViewModel.updateRotationThreshold(2.0)
                            } else {
                                fallDetectionViewModel.updateAccelerationThreshold(25.0)
                                fallDetectionViewModel.updateRotationThreshold(3.0)
                            }
                        }
                    }
                )

                ModernDivider()

                // Location Services
                ModernToggleItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location Services",
                    description = "Share location with emergency contacts",
                    isEnabled = locationEnabled,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onToggle = {
                        if (locationEnabled) {
                            coroutineScope.launch {
                                settingsManager.setLocationEnabled(false)
                                val locationService = LocationService.getInstance(context)
                                locationService.stopLocationUpdates()
                            }
                        } else {
                            // Request location permissions
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                ModernDivider()

                // Auto Emergency Call
                ModernToggleItem(
                    icon = Icons.Default.Call,
                    title = "Auto Emergency Call",
                    description = "Call emergency contacts automatically",
                    isEnabled = autoEmergencyCall,
                    iconBackgroundColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFD32F2F),
                    onToggle = {
                        coroutineScope.launch {
                            settingsManager.setAutoEmergencyCall(!autoEmergencyCall)
                        }
                    }
                )

                ModernDivider()

                // Auto Emergency SMS
                ModernToggleItem(
                    icon = Icons.Default.Warning,
                    title = "Auto Emergency SMS",
                    description = "Send SMS automatically when fall detected",
                    isEnabled = autoEmergencySms,
                    iconBackgroundColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFFF6F00),
                    onToggle = {
                        coroutineScope.launch {
                            val newState = !autoEmergencySms

                            // Check SMS permission before enabling
                            if (newState) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.SEND_SMS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    settingsManager.setAutoEmergencySms(newState)
                                    Toast.makeText(
                                        context,
                                        "Auto Emergency SMS enabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Request SMS permission
                                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                                }
                            } else {
                                settingsManager.setAutoEmergencySms(newState)
                                Toast.makeText(
                                    context,
                                    "Auto Emergency SMS disabled. WhatsApp will still be available.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }

            // Detection Stats Card
            DetectionStatsCard(
                fallsDetected = uiState.fallCount,
                isMonitoring = uiState.isMonitoring,
                threshold = uiState.accelerationThreshold
            )


            // Notifications Section
            ModernSection(
                title = "Notifications",
                icon = Icons.Default.Notifications,
                iconColor = MaterialTheme.colorScheme.primary
            ) {
                ModernToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    description = if (systemNotificationsEnabled) {
                        "Receive alerts and updates"
                    } else {
                        "Tap to enable in system settings"
                    },
                    isEnabled = notifications && systemNotificationsEnabled,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onToggle = {
                        if (!systemNotificationsEnabled) {
                            // Open system notification settings
                            notificationHelper.openNotificationSettings()
                        } else {
                            coroutineScope.launch {
                                val newState = !notifications
                                settingsManager.setNotificationsEnabled(newState)

                                // Send test notification when enabling
                                if (newState) {
                                    notificationHelper.sendTestNotification()
                                }
                            }
                        }

                        // Update system notification status
                        systemNotificationsEnabled = notificationHelper.areNotificationsEnabled()
                    }
                )

                ModernDivider()

                // Warning when system notifications are disabled
                if (!systemNotificationsEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notifications Disabled",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Enable in system settings to receive fall alerts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    ModernDivider()
                }

                ModernToggleItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound Alerts",
                    description = "Play sounds for fall detection",
                    isEnabled = soundAlerts,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onToggle = {
                        coroutineScope.launch {
                            settingsManager.setSoundAlerts(!soundAlerts)
                        }
                    }
                )

                ModernDivider()

                ModernToggleItem(
                    icon = Icons.Default.Warning,
                    title = "Loud Emergency Alerts",
                    description = "Maximum volume during countdown",
                    isEnabled = true,
                    iconBackgroundColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFF57C00),
                    onToggle = { }
                )
            }

            // Emergency Contacts
//            EmergencyContactCard(
//                contactInfo = currentUser?.emergencyContact ?: "Not set",
//                onClick = { /* Navigate to emergency contact screen */ }
//            )

            // Privacy & Data Section
            ModernSection(
                title = "Privacy & Data",
                icon = Icons.Default.PrivacyTip,
                iconColor = MaterialTheme.colorScheme.tertiary
            ) {
                ModernToggleItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Share Anonymous Data",
                    description = "Help improve fall detection",
                    isEnabled = shareData,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onToggle = {
                        coroutineScope.launch {
                            settingsManager.setShareData(!shareData)
                        }
                    }
                )

                ModernDivider()

                ModernToggleItem(
                    icon = Icons.Default.History,
                    title = "Keep Fall History",
                    description = "${uiState.fallHistory.size} events recorded",
                    isEnabled = true,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onToggle = { }
                )

                ModernDivider()

                ModernActionItem(
                    icon = Icons.Default.Download,
                    title = "Export Safety Data",
                    description = "Download fall history (${uiState.fallHistory.size} events)",
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    actionText = "Export",
                    onClick = {
                        ExportUtil.exportFallHistoryToCSV(
                            context = context,
                            fallHistory = uiState.fallHistory
                        )
                    }
                )
            }

            // Appearance Section
            ModernSection(
                title = "Appearance",
                icon = Icons.Default.Palette,
                iconColor = MaterialTheme.colorScheme.secondary
            ) {
                ModernToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    description = "Switch between light and dark mode",
                    isEnabled = darkTheme,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onToggle = {
                        coroutineScope.launch {
                            settingsManager.setDarkTheme(!darkTheme)
                        }
                    }
                )

                ModernDivider()

                ModernActionItem(
                    icon = Icons.Default.Palette,
                    title = "Theme Color",
                    description = "Customize app colors",
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    actionText = "System",
                    onClick = { }
                )
            }

            // App Information
//            AppInfoCard(
//                version = "1.0.0",
//                userId = currentUser?.email ?: "Not available",
//                deviceModel = Build.MODEL,
//                androidVersion = Build.VERSION.RELEASE
//            )

            // Support Section
//            ModernSection(
//                title = "Support & Help",
//                icon = Icons.AutoMirrored.Filled.Help,
//                iconColor = MaterialTheme.colorScheme.primary
//            ) {
//                ModernNavigationItem(
//                    icon = Icons.AutoMirrored.Filled.Help,
//                    title = "Help & Tutorials",
//                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
//                    iconTint = MaterialTheme.colorScheme.primary,
//                    onClick = { }
//                )
//
//                ModernDivider()
//
//                ModernNavigationItem(
//                    icon = Icons.Default.BugReport,
//                    title = "Report Issue",
//                    iconBackgroundColor = Color(0xFFFFF3E0),
//                    iconTint = Color(0xFFF57C00),
//                    onClick = { }
//                )
//
//                ModernDivider()
//
//                ModernNavigationItem(
//                    icon = Icons.Default.PrivacyTip,
//                    title = "Privacy Policy",
//                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
//                    iconTint = MaterialTheme.colorScheme.secondary,
//                    onClick = { }
//                )
//
//                ModernDivider()
//
//                ModernNavigationItem(
//                    icon = Icons.Default.Description,
//                    title = "Terms of Service",
//                    iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//                    iconTint = MaterialTheme.colorScheme.tertiary,
//                    onClick = { }
//                )
//            }

            // Account Actions Section
            ModernSection(
                title = "Account",
                icon = Icons.Default.Person,
                iconColor = MaterialTheme.colorScheme.primary
            ) {
                ModernNavigationItem(
                    icon = Icons.Default.Call,
                    title = "Manage Emergency Contacts",
                    description = "Add or edit emergency contacts",
                    iconBackgroundColor = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFD32F2F),
                    onClick = {
                        navController.navigate("emergency_contact")
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            FilledTonalButton(
                onClick = {
                    authViewModel.logout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UserProfileHeader(email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = email.substringBefore("@"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ModernSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ModernToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    iconBackgroundColor: Color,
    iconTint: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = iconTint,
                checkedTrackColor = iconBackgroundColor
            )
        )
    }
}

@Composable
fun ModernActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconBackgroundColor: Color,
    iconTint: Color,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(actionText)
        }
    }
}

@Composable
fun ModernNavigationItem(
    icon: ImageVector,
    title: String,
    iconBackgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    description: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DetectionStatsCard(
    fallsDetected: Int,
    isMonitoring: Boolean,
    threshold: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMonitoring)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detection Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isMonitoring)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.outline
                ) {
                    Text(
                        text = if (isMonitoring) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Falls Detected", fallsDetected.toString())
                StatItem("Threshold", "${threshold.toInt()} m/s²")
                StatItem("Accuracy", if (threshold < 23.0) "High" else "Normal")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmergencyContactCard(
    contactInfo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contactInfo != "Not set")
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (contactInfo != "Not set")
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.error
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = if (contactInfo != "Not set")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (contactInfo != "Not set") contactInfo else "⚠️ No contact set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (contactInfo != "Not set")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Manage",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppInfoCard(
    version: String,
    userId: String,
    deviceModel: String,
    androidVersion: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoRow("Version", version)
            InfoRow("User ID", userId)
            InfoRow("Device", deviceModel)
            InfoRow("Android", androidVersion)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF4CAF50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Operational",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

