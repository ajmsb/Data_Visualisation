package com.example.practicaltrainingproject.presentation.ui.mainScreen.Components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.max


@Composable
fun GyroscopeChart() {

    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val gyroscope = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }
    var z by remember { mutableFloatStateOf(0f) }

    // Buffers to hold chart points
    val xData = remember { mutableStateListOf<Float>() }
    val yData = remember { mutableStateListOf<Float>() }
    val zData = remember { mutableStateListOf<Float>() }

    val maxPoints = 100

    // SensorEventListener
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    x = it.values[0]
                    y = it.values[1]
                    z = it.values[2]

                    xData.add(x)
                    yData.add(y)
                    zData.add(z)

                    if (xData.size > maxPoints) xData.removeAt(0)
                    if (yData.size > maxPoints) yData.removeAt(0)
                    if (zData.size > maxPoints) zData.removeAt(0)

                    //Log.d("Gyroscope", "X: $x, Y: $y, Z: $z")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gyroscope Readings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = Color.Red)
        Text("Y: $y", color = Color.Green)
        Text("Z: $z", color = Color.Blue)
        Spacer(modifier = Modifier.height(16.dp))
        AxisCanvasChart("X Axis", xData, Color.Red)
        Spacer(modifier = Modifier.height(8.dp))
        AxisCanvasChart("Y Axis", yData, Color.Green)
        Spacer(modifier = Modifier.height(8.dp))
        AxisCanvasChart("Z Axis", zData, Color.Blue)
    }
}

// Canvas Charts Composable
@Composable
fun AxisCanvasChart(
    title: String,
    data: List<Float>,
    color: Color
) {
    val maxY = 10f
    val minY = -10f

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
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

                drawPath(path, color = color, style = Stroke(4f))
            }
        }
    }
}
