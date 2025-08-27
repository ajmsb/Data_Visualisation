package com.example.practicaltrainingproject.presentation.ui.mainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practicaltrainingproject.presentation.ui.mainScreen.Components.SensorChartsSharedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Data App") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = { navController.navigate("dashboard") },
                    modifier = Modifier.padding(8.dp),
                    enabled = currentRoute != "dashboard"
                ) {
                    Text("Dashboard")
                }
                Button(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.padding(8.dp),
                    enabled = currentRoute != "profile"
                ) {
                    Text("Profile")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen()
            }
            composable("profile") {
                com.example.practicaltrainingproject.presentation.ui.profileScreen.ProfileScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.width(10.dp))
            SensorChartsSharedState()
        }
    }
}
