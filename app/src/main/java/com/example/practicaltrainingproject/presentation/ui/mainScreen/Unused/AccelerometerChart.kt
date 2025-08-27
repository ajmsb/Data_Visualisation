package com.example.practicaltrainingproject.presentation.ui.mainScreen.Unused

import android.content.Context
import android.graphics.Paint
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.data.local.WeeklyImpactData
import com.example.practicaltrainingproject.data.model.WeeklyImpactStore
import com.example.practicaltrainingproject.presentation.ui.mainScreen.Components.WeeklyImpactChartFromState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.sqrt


@Composable
fun AccelerometerCharts() {

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

    // Holds impact counts for Mon..Sun
    val dailyImpactCounts = remember {
        mutableStateListOf(0, 0, 0, 0, 0, 0, 0)
    }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val store = remember { WeeklyImpactStore(context) }
    val scope = rememberCoroutineScope()

// Collect existing DataStore values initially
    LaunchedEffect(Unit) {
        store.weeklyData.collect { data ->
            days.forEachIndexed { index, day ->
                dailyImpactCounts[index] = data.counts[day] ?: 0
            }
        }
    }

    // SensorEventListener
    DisposableEffect(Unit) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
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

                    //Calculating total acceleration
                    val totalAcc = sqrt(x * x + y * y + z * z) / 10
                    // Get user-defined threshold
                    val threshold = prefs.getFloat("sensitivity_threshold", 5f)
                    if (totalAcc > threshold) {
                        Log.w("IMPACT", "⚠️ Impact detected: ${"%.2f".format(totalAcc)}g")
                        val today = LocalDate.now().dayOfWeek.value % 7 // Mon=1 → index 0
                        dailyImpactCounts[today] = dailyImpactCounts[today] + 1
                        scope.launch {
                            store.saveWeeklyData(WeeklyImpactData(
                                counts = days.zip(dailyImpactCounts).toMap()
                            ))
                        }
                    } else {
                        Log.v("SENSOR", "Normal reading: ${"%.2f".format(totalAcc)}g")
                    }
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
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sensor Readings ",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        WeeklyImpactChartFromState(days, dailyImpactCounts)

        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = Color.Red)
        Text("Y: $y", color = Color.Green)
        Text("Z: $z", color = Color.Blue)
        Spacer(modifier = Modifier.height(16.dp))
        AxisChart(title = "X Axis", color = Color.Red, data = xData)
        Spacer(modifier = Modifier.height(8.dp))
        AxisChart(title = "Y Axis", color = Color.Green, data = yData)
        Spacer(modifier = Modifier.height(8.dp))
        AxisChart(title = "Z Axis", color = Color.Blue, data = zData)
        Spacer(modifier = Modifier.height(16.dp))

//        Text(
//            "Vico 2.1.2 Charts",
//            style = MaterialTheme.typography.headlineSmall,
//            color = MaterialTheme.colorScheme.primary
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        LiveAxisChart("X Axis", xData, Color.Red)
//        Spacer(modifier = Modifier.height(8.dp))
//        LiveAxisChart("Y Axis", yData, Color.Green)
//        Spacer(modifier = Modifier.height(8.dp))
//        LiveAxisChart("Z Axis", zData, Color.Blue)
    }
}






/*
@Composable
fun WeeklyImpactChart(store: WeeklyImpactStore) {
    val weeklyData by store.weeklyData.collectAsState(
        initial = WeeklyImpactData(
            counts = mapOf(
                "Mon" to 0, "Tue" to 0, "Wed" to 0,
                "Thu" to 0, "Fri" to 0, "Sat" to 0, "Sun" to 0
            )
        )
    )

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxCount = (weeklyData.counts.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFEFEFEF))
            .padding(horizontal = 8.dp)
    ) {
        val barWidth = size.width / (days.size * 1.5f)
        days.forEachIndexed { i, day ->
            val count = (weeklyData.counts[day] ?: 0).toFloat()
            val barHeight = (count / maxCount) * size.height

            // Draw bar
            drawRect(
                color = Color(0xFF3490DE),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = i * barWidth * 1.5f,
                    y = size.height - barHeight
                ),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )

            // Draw label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    day,
                    i * barWidth * 1.5f + barWidth / 4,
                    size.height - 4,
                    android.graphics.Paint().apply {
                        textSize = 28f
                        color = android.graphics.Color.BLACK
                    }
                )
            }
        }
    }
}

*/


// Canvas BAR CHart
@Composable
fun DailyImpactChart(
    dailyCounts: List<Int>,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxCount = (dailyCounts.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Weekly Impact Count", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF))
                .padding(horizontal = 8.dp)
        ) {
            val barWidth = size.width / (days.size * 2f)
            dailyCounts.forEachIndexed { index, count ->
                val barHeight = (count / maxCount.toFloat()) * size.height
                val left = index * (2 * barWidth) + barWidth / 2
                val top = size.height - barHeight

                // Draw bar
                drawRect(
                    color = Color(0xFF3490DE),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight)
                )

                // Draw label under bar


                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        days[index],
                        left + barWidth / 4,
                        size.height - 5,
                        Paint().apply {
                            textSize = 28f
                            color = android.graphics.Color.BLACK
                        }
                    )
                }
            }
        }
    }
}



// Canvas Charts Composable
@Composable
fun AxisChart(title: String,
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
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}


// Vico Chart Composable
@Composable
fun LiveAxisChart(
    title: String,
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {

    val modelProducer = remember { CartesianChartModelProducer() }


    var lastUpdate by remember { mutableLongStateOf(0L) }


    val chartData by remember(data) {
        derivedStateOf {
            data.takeLast(100)
        }
    }

    LaunchedEffect(chartData) {

        if (chartData.isNotEmpty() && System.currentTimeMillis() - lastUpdate > 50) {
            try {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = chartData.indices.map { it.toFloat() },
                            y = chartData
                        )
                    }
                }
                lastUpdate = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("LiveAxisChart", "Error updating chart", e)
            }
        }
    }

    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.bodyLarge)

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill(color)),
                            areaFill =
                                LineCartesianLayer.AreaFill.single(
                                    fill(
                                        Color.LightGray
                                    )
                                ),
                        )
                    )
                ),
                bottomAxis = HorizontalAxis.rememberBottom(),
                startAxis = VerticalAxis.rememberStart()
            ),
            modelProducer,
            modifier.height(120.dp),
            rememberVicoScrollState(scrollEnabled = false),
        )
    }
}
