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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.random.Random


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
    val model = entryModelOf(entriesOf(4f, 12f, 8f, 16f), entriesOf(12f, 16f, 4f, 12f))
    fun getRandomEntries() = List(4) { entryOf(it, Random.nextFloat() * 16f) }
    val chartEntryModelProducer = ChartEntryModelProducer(getRandomEntries())
    
    // Display the line chart
    Chart(
        chart = lineChart(), // use barChart(), columnChart() for others
        model = model,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
    )
}


