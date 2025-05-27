package com.example.practicaltrainingproject.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.Components.BarChart
import com.example.practicaltrainingproject.Components.LineChart
import com.example.practicaltrainingproject.Components.SensorDataChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Practical Training Project")
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.secondaryContainer) { }
        }
    ) { innerPadding ->
        LazyColumn(

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 10.dp)
        ) {
            item {
                Text("Calories Chart")
                LineChart(modifier = Modifier.padding(10.dp))
                Spacer(modifier = Modifier.width(10.dp))
                BarChart(modifier = Modifier.padding(10.dp))
                Spacer(modifier = Modifier.width(10.dp))
                SensorDataChart()
            }


        }
    }

}