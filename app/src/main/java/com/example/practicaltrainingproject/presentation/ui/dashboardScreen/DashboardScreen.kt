package com.example.practicaltrainingproject.presentation.ui.dashboardScreen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.presentation.ui.mainScreen.components.SensorChartsSharedState

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