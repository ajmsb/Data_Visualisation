package com.example.practicaltrainingproject.presentation.ui.mainScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practicaltrainingproject.data.auth.AuthState
import com.example.practicaltrainingproject.presentation.ui.components.LoadingScreen
import com.example.practicaltrainingproject.presentation.ui.loginScreen.LoginScreen
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel
import com.example.practicaltrainingproject.presentation.ui.signup.SignupScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Show loading screen during authentication process
    if (authState is AuthState.Loading) {
        LoadingScreen()
        return
    }

    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // Navigate to main app when authenticated
                navController.navigate("main_app") {
                    popUpTo(0) { inclusive = true }
                }
            }

            is AuthState.Unauthenticated -> {
                // Navigate back to login when user logs out
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }

            else -> {
                // Handle other states (Loading, Error, etc.)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // Signup Screen
        composable("signup") {
            SignupScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // Main App Screens - This is where the shared ViewModel should be created
        composable("main_app") {
            MainAppContent(navController, authViewModel)
        }

        // Note: Individual screens (dashboard, fall_detection, profile, settings) are
        // handled inside MainAppContent with a shared FallDetectionViewModel.
        // Direct navigation to these screens is not supported at this level.
    }
}