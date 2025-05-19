package com.example.practicaltrainingproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.practicaltrainingproject.ui.theme.PracticalTrainingProjectTheme
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticalTrainingProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Text("Hello")
                        SimpleLineChart()
                    }
                }
            }
        }
    }
}


@Composable
fun SimpleLineChart() {
    // Create data points for the chart
    val model = entryModelOf(1f, 3f, 5f, 4f, 6f, 7f, 5f)

    // Display the line chart
    Chart(
        chart = lineChart(), // use barChart(), columnChart() for others
        model = model
    )
}


