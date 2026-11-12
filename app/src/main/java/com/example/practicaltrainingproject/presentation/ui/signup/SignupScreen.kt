package com.example.practicaltrainingproject.presentation.ui.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.practicaltrainingproject.presentation.ui.loginScreen.domain.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = {
                        authViewModel.resetTempData()
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Login"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SignupFlow(
                authViewModel = authViewModel,
                onBackToLogin = {
                    authViewModel.resetTempData()
                    navController.popBackStack()
                },
                onSignupComplete = {
                    // Registration is handled by AuthViewModel
                    // Navigation will be triggered by MainScreen's LaunchedEffect
                }
            )
        }
    }
}