package com.example.practicaltrainingproject.presentation.ui.mainScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.practicaltrainingproject.data.manager.SettingsManager
import com.example.practicaltrainingproject.presentation.ui.dashboardScreen.DashboardScreen
import com.example.practicaltrainingproject.presentation.ui.emergencyContactScreen.ManageEmergencyContactsScreen
import com.example.practicaltrainingproject.presentation.ui.fallDetectionScreen.FallDetectionScreen
import com.example.practicaltrainingproject.presentation.ui.fallHistoryScreen.FallHistoryScreen
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel
import com.example.practicaltrainingproject.presentation.ui.profileScreen.ProfileScreen
import com.example.practicaltrainingproject.presentation.ui.settingsScreen.SettingsScreen
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallDetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    mainNavController: NavHostController,
    authViewModel: AuthViewModel
) {
    // Create the shared FallDetectionViewModel
    val fallDetectionViewModel: FallDetectionViewModel = viewModel()

    // Get context and settings
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val autoEmergencySms by settingsManager.autoEmergencySms.collectAsState(initial = true)

    // Collect user data and fall detection state
    val currentUser by authViewModel.currentUser.collectAsState()
    val fallDetectionState by fallDetectionViewModel.uiState.collectAsState()

    // Monitor for fall confirmation and send WhatsApp alert
    LaunchedEffect(fallDetectionState.lastFallTimestamp) {
        // When lastFallTimestamp changes, a new fall was confirmed
        if (fallDetectionState.lastFallTimestamp != "Never" &&
            fallDetectionState.fallHistory.isNotEmpty()
        ) {

            val latestFall = fallDetectionState.fallHistory.lastOrNull()
            if (latestFall != null && !latestFall.wasCancelled) {
                // Fall was confirmed, send WhatsApp message with SMS setting to ALL contacts
                currentUser?.let { user ->
                    fallDetectionViewModel.sendWhatsAppEmergencyAlert(
                        userName = user.name,
                        emergencyContact = user.emergencyContact,
                        emergencyContacts = user.emergencyContacts,
                        autoSmsEnabled = autoEmergencySms
                    )
                }
            }
        }
    }

    // Create a separate nav controller for the main app screens
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = {
                        Text(
                            "Dashboard",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        if (currentRoute != "dashboard") {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Fall Detection"
                        )
                    },
                    label = {
                        Text(
                            "Detection",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentRoute == "fall_detection",
                    onClick = {
                        if (currentRoute != "fall_detection") {
                            navController.navigate("fall_detection") {
                                launchSingleTop = true
                            }
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentRoute == "profile",
                    onClick = {
                        if (currentRoute != "profile") {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        }
                    }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text(
                            "Settings",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentRoute == "settings",
                    onClick = {
                        if (currentRoute != "settings") {
                            navController.navigate("settings") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    fallDetectionViewModel = fallDetectionViewModel
                )
            }
            composable("fall_detection") {
                FallDetectionScreen(fallDetectionViewModel = fallDetectionViewModel)
            }
            composable("profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
            composable("settings") {
                SettingsScreen(
                    authViewModel = authViewModel,
                    fallDetectionViewModel = fallDetectionViewModel,
                    navController = navController
                )
            }

            composable("fall_history") {
                FallHistoryScreen(
                    navController = navController,
                    fallDetectionViewModel = fallDetectionViewModel,
                    authViewModel = authViewModel
                )
            }

            composable("emergency_contact") {
                ManageEmergencyContactsScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}