package com.example.practicaltrainingproject.Components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun SensorDataChart() {

    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }
    var z by remember { mutableFloatStateOf(0f) }

    // Buffers to hold chart points
    val xData = remember { mutableStateListOf<Float>() }
    val yData = remember { mutableStateListOf<Float>() }
    val zData = remember { mutableStateListOf<Float>() }

    val maxPoints = 100

    // SensorEventListener lifecycle-aware
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    x = it.values[0]
                    y = it.values[1]
                    z = it.values[2]

                    xData.add(x)
                    yData.add(y)
                    zData.add(z)

                    if (xData.size > maxPoints) xData.removeFirst()
                    if (yData.size > maxPoints) yData.removeFirst()
                    if (zData.size > maxPoints) zData.removeFirst()

                    Log.d("Accelerometer", "X: $x, Y: $y, Z: $z")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // UI Layout
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            "Accelerometer Readings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = MaterialTheme.colorScheme.primary)
        Text("Y: $y", color = MaterialTheme.colorScheme.primary)
        Text("Z: $z", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        AxisChart(title = "X Axis", color = Color.Red, data = xData)
        Spacer(modifier = Modifier.height(16.dp))
        AxisChart(title = "Y Axis", color = Color.Green, data = yData)
        Spacer(modifier = Modifier.height(16.dp))
        AxisChart(title = "Z Axis", color = Color.Blue, data = zData)
    }
}

// Charts Composable
@Composable
fun AxisChart(
    title: String,
    color: Color,
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val maxY = 15f
    val minY = -15f

    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF))
        ) {
            if (data.isNotEmpty()) {
                val path = Path()
                val stepX = size.width / max(1, data.size - 1)
                val centerY = size.height / 2

                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = centerY - (value / (maxY - minY) * size.height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun PrevSensorData() {
    SensorDataChart()
}