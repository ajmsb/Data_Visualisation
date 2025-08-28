package com.example.practicaltrainingproject.presentation.ui.mainScreen.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.data.local.HourlyImpactData
import com.example.practicaltrainingproject.data.local.WeeklyImpactData
import com.example.practicaltrainingproject.data.model.HourlyImpactStore
import com.example.practicaltrainingproject.data.model.WeeklyImpactStore
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.sqrt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SensorChartsSharedState() {
    val owner = LocalViewModelStoreOwner.current
    val context = LocalContext.current
    if (owner == null) {
        // Show a fallback UI or error if the ViewModelStoreOwner is not available
        Text("Error: ViewModelStoreOwner is not available.")
        return
    }
    val viewModel: SensorChartsViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = SensorChartsViewModel.Factory(context)
    )
    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gyroscope = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    // Shared state
    var accX by remember { mutableFloatStateOf(0f) }
    var accY by remember { mutableFloatStateOf(0f) }
    var accZ by remember { mutableFloatStateOf(0f) }
    val accXData = remember { mutableStateListOf<Float>() }
    val accYData = remember { mutableStateListOf<Float>() }
    val accZData = remember { mutableStateListOf<Float>() }
    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }
    val gyroXData = remember { mutableStateListOf<Float>() }
    val gyroYData = remember { mutableStateListOf<Float>() }
    val gyroZData = remember { mutableStateListOf<Float>() }
    val maxPoints = 100
    val dailyImpactCounts = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }
    val hourlyStore = remember { HourlyImpactStore(context) }
    val hourlyImpactCounts = remember { mutableStateListOf(*Array(24) { 0 }) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = (0..23).map { it.toString() }
    val store = remember { WeeklyImpactStore(context) }
    val coroutineScope = rememberCoroutineScope()

    // Peak times state
    val dailyPeakTimes = remember { List(7) { mutableStateListOf<LocalTime>() } }
    val hourlyPeakTimes = remember { List(24) { mutableStateListOf<LocalTime>() } }

    // Weekly impact data effect
    LaunchedEffect(Unit) {
        store.weeklyData.collect { data ->
            days.forEachIndexed { index, day ->
                dailyImpactCounts[index] = data.counts[day] ?: 0
            }
        }
    }

    // Hourly impact data effect
    LaunchedEffect(Unit) {
        hourlyStore.hourlyData.collect { data ->
            hours.forEachIndexed { index, hour ->
                hourlyImpactCounts[index] = data.counts[hour] ?: 0
            }
        }
    }

    // Accelerometer effect
    DisposableEffect(accelerometer) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val accListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    accX = it.values[0]
                    accY = it.values[1]
                    accZ = it.values[2]
                    accXData.add(accX)
                    accYData.add(accY)
                    accZData.add(accZ)
                    if (accXData.size > maxPoints) accXData.removeAt(0)
                    if (accYData.size > maxPoints) accYData.removeAt(0)
                    if (accZData.size > maxPoints) accZData.removeAt(0)
                    val totalAcc = sqrt(accX * accX + accY * accY + accZ * accZ) / 10
                    val threshold = prefs.getFloat("sensitivity_threshold", 5f)
                    if (totalAcc > threshold) {
                        viewModel.addImpactValue(totalAcc)
                        val today = java.time.LocalDate.now().dayOfWeek.value - 1 // Monday=0, Sunday=6
                        dailyImpactCounts[today] = dailyImpactCounts[today] + 1
                        val currentHour = LocalTime.now().hour
                        hourlyImpactCounts[currentHour] = hourlyImpactCounts[currentHour] + 1
                        // Record peak times
                        val now = LocalTime.now()
                        dailyPeakTimes[today].add(now)
                        hourlyPeakTimes[currentHour].add(now)
                        coroutineScope.launch {
                            store.saveWeeklyData(
                                WeeklyImpactData(
                                    counts = days.zip(dailyImpactCounts).toMap()
                                )
                            )
                            hourlyStore.saveHourlyData(
                                HourlyImpactData(
                                    counts = hours.zip(hourlyImpactCounts).toMap()
                                )
                            )
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(accListener) }
    }

    // Gyroscope effect
    DisposableEffect(gyroscope) {
        val gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    gyroX = it.values[0]
                    gyroY = it.values[1]
                    gyroZ = it.values[2]
                    gyroXData.add(gyroX)
                    gyroYData.add(gyroY)
                    gyroZData.add(gyroZ)
                    if (gyroXData.size > maxPoints) gyroXData.removeAt(0)
                    if (gyroYData.size > maxPoints) gyroYData.removeAt(0)
                    if (gyroZData.size > maxPoints) gyroZData.removeAt(0)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(gyroListener) }
    }

    // Threshold chart state for live updates
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val savedThreshold = prefs.getFloat("sensitivity_threshold", 5f)
    var threshold by remember { mutableStateOf<Float>(savedThreshold) }
    val thresholdHistory = remember { mutableStateListOf<Float>() }
    val thresholdChartModelProducer = remember { CartesianChartModelProducer() }
    // Ensure at least two values for chart rendering
    LaunchedEffect(Unit) {
        if (thresholdHistory.isEmpty()) {
            thresholdHistory.add(threshold)
            thresholdHistory.add(threshold) // Add twice for a visible line
            thresholdChartModelProducer.runTransaction {
                lineSeries { series((0 until thresholdHistory.size).toList(), thresholdHistory) }
            }
        }
    }
    // Update chart data when threshold changes
    LaunchedEffect(threshold) {
        if (thresholdHistory.isNotEmpty() && thresholdHistory.last() != threshold) {
            thresholdHistory.add(threshold)
            thresholdChartModelProducer.runTransaction {
                lineSeries { series((0 until thresholdHistory.size).toList(), thresholdHistory) }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        AccelerometerChart(
            days = days, dailyImpactCounts = dailyImpactCounts,
            hours = hours, hourlyImpactCounts = hourlyImpactCounts,
            impactSensitivityHistory = viewModel.impactSensitivityHistory
        )
        Spacer(modifier = Modifier.height(32.dp))
        GyroscopeChart(
            x = gyroX, y = gyroY, z = gyroZ
        )
        Spacer(modifier = Modifier.height(32.dp))
        PeakDetectionChart(
            days = days,
            dailyPeakTimes = dailyPeakTimes,
            hours = hours,
            hourlyPeakTimes = hourlyPeakTimes
        )
    }
}

@Composable
fun ImpactSensitivityCanvasChart(impactSensitivityHistory: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF4287F4)
    val dotColor = Color(0xFFF44266)
    val textColor = Color.Black
    val maxValue = (impactSensitivityHistory.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minValue = (impactSensitivityHistory.minOrNull() ?: 0f)
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 40f
            val leftPadding = 40f
            val chartHeight = canvasHeight - bottomPadding
            val chartWidth = canvasWidth - leftPadding
            // Title
            val chartTitle = "Impact Sensitivity Over Time"
            val titleX = canvasWidth / 2
            val titleY = 20f
            drawContext.canvas.nativeCanvas.drawText(
                chartTitle,
                titleX,
                titleY,
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 36f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
            // Draw Y axis
            drawLine(
                color = Color.Gray,
                start = Offset(leftPadding, 0f),
                end = Offset(leftPadding, chartHeight),
                strokeWidth = 2f
            )
            // Draw X axis
            drawLine(
                color = Color.Gray,
                start = Offset(leftPadding, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                strokeWidth = 2f
            )
            // Draw Y axis labels (min, max)
            drawContext.canvas.nativeCanvas.drawText(
                "${minValue}",
                5f,
                chartHeight,
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 28f
                }
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${maxValue}",
                5f,
                30f,
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 28f
                }
            )
            // Draw line chart
            if (impactSensitivityHistory.size > 1) {
                val path = Path()
                impactSensitivityHistory.forEachIndexed { i, value ->
                    val x = leftPadding + i * (chartWidth / (impactSensitivityHistory.size - 1).coerceAtLeast(1))
                    val y = chartHeight - ((value - minValue) / (maxValue - minValue).coerceAtLeast(1e-6f) * (chartHeight - 30f))
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(dotColor, radius = 6f, center = Offset(x, y))
                }
                drawPath(path, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
fun AccelerometerChart(
    days: List<String>, dailyImpactCounts: List<Int>,
    hours: List<String>, hourlyImpactCounts: List<Int>,
    impactSensitivityHistory: List<Float>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "Accelerometer Readings ",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        /*Text("X: $x", color = MaterialTheme.colorScheme.error)
        Text("Y: $y", color = MaterialTheme.colorScheme.secondary)
        Text("Z: $z", color = MaterialTheme.colorScheme.tertiary)*/
        Spacer(modifier = Modifier.height(16.dp))
        WeeklyImpactChartFromState(days, dailyImpactCounts)
        Spacer(modifier = Modifier.height(32.dp))
        HourlyImpactChartFromState(hours, hourlyImpactCounts)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Impact Sensitivity Chart",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        ImpactSensitivityCanvasChart(impactSensitivityHistory = impactSensitivityHistory)
    }
}

@Composable
fun GyroscopeChart(
    x: Float, y: Float, z: Float
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "Gyroscope Readings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = MaterialTheme.colorScheme.error)
        Text("Y: $y", color = MaterialTheme.colorScheme.secondary)
        Text("Z: $z", color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun PeakDetectionChart(
    days: List<String>,
    dailyPeakTimes: List<List<LocalTime>>,
    hours: List<String>,
    hourlyPeakTimes: List<List<LocalTime>>
) {
    // Prepare data for charts
    val dailyCounts = days.mapIndexed { i, _ -> dailyPeakTimes[i].size.toFloat() }
    val hourlyCounts = hours.mapIndexed { i, _ -> hourlyPeakTimes[i].size.toFloat() }
    val dailyModelProducer = remember { CartesianChartModelProducer() }
    val hourlyModelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(dailyCounts) {
        dailyModelProducer.runTransaction {
            lineSeries { series((0 until days.size).toList(), dailyCounts) }
        }
    }
    LaunchedEffect(hourlyCounts) {
        hourlyModelProducer.runTransaction {
            lineSeries { series((0 until hours.size).toList(), hourlyCounts) }
        }
    }
    val blueLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(Color.Blue))
    )
    val redLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(Color.Red))
    )

    // Custom marker composable
    @Composable
    fun PeakMarker(hourIndex: Int): CartesianMarker {
        val times = if (hourIndex in hourlyPeakTimes.indices) hourlyPeakTimes[hourIndex] else emptyList()
        val label = if (times.isNotEmpty()) times.joinToString(", ") { it.toString() } else "No peaks"
        return DefaultCartesianMarker(label = rememberTextComponent(), valueFormatter = { _, _ -> label })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Peak Detection Chart",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Peaks per Day (Line Chart):", style = MaterialTheme.typography.bodyMedium)
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(blueLine)
                ),
                marker = PeakMarker(0)
            ),
            modelProducer = dailyModelProducer,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Peaks per Hour (Line Chart):", style = MaterialTheme.typography.bodyMedium)
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(redLine)
                ),
                marker = DefaultCartesianMarker(label = rememberTextComponent())
            ),
            modelProducer = hourlyModelProducer,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Optionally, keep the text summary below
        Text("Peaks per Day (Summary):", style = MaterialTheme.typography.bodyMedium)
        days.forEachIndexed { index, day ->
            val times = dailyPeakTimes[index].joinToString { it.toString() }
            Text(
                "$day: ${dailyPeakTimes[index].size} peaks",
                style = MaterialTheme.typography.bodySmall
            )
            if (dailyPeakTimes[index].isNotEmpty()) {
                Text("Times: $times", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Peaks per Hour (Summary):", style = MaterialTheme.typography.bodyMedium)
        hours.forEachIndexed { index, hour ->
            val times = hourlyPeakTimes[index].joinToString { it.toString() }
            Text(
                "$hour: ${hourlyPeakTimes[index].size} peaks",
                style = MaterialTheme.typography.bodySmall
            )
            if (hourlyPeakTimes[index].isNotEmpty()) {
                Text("Times: $times", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
