package com.example.practicaltrainingproject.presentation.ui.mainScreen.Components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.practicaltrainingproject.data.local.HourlyImpactData
import com.example.practicaltrainingproject.data.local.WeeklyImpactData
import com.example.practicaltrainingproject.data.model.HourlyImpactStore
import com.example.practicaltrainingproject.data.model.WeeklyImpactStore
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun SensorChartsSharedState() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
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
                        val today = java.time.LocalDate.now().dayOfWeek.value - 1 // Monday=0, Sunday=6
                        dailyImpactCounts[today] = dailyImpactCounts[today] + 1
                        val currentHour = java.time.LocalTime.now().hour
                        hourlyImpactCounts[currentHour] = hourlyImpactCounts[currentHour] + 1
                        coroutineScope.launch {
                            store.saveWeeklyData(WeeklyImpactData(
                                counts = days.zip(dailyImpactCounts).toMap()
                            ))
                            hourlyStore.saveHourlyData(HourlyImpactData(
                                counts = hours.zip(hourlyImpactCounts).toMap()
                            ))
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AccelerometerChart(
            x = accX, y = accY, z = accZ,
            days = days, dailyImpactCounts = dailyImpactCounts,
            hours = hours, hourlyImpactCounts = hourlyImpactCounts
        )
        Spacer(modifier = Modifier.height(32.dp))
        GyroscopeChart(
            x = gyroX, y = gyroY, z = gyroZ
        )
    }
}

@Composable
fun AccelerometerChart(
    x: Float, y: Float, z: Float,
    days: List<String>, dailyImpactCounts: List<Int>,
    hours: List<String>, hourlyImpactCounts: List<Int>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Accelerometer Readings ", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = MaterialTheme.colorScheme.error)
        Text("Y: $y", color = MaterialTheme.colorScheme.secondary)
        Text("Z: $z", color = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(16.dp))
        WeeklyImpactChartFromState(days, dailyImpactCounts)
        Spacer(modifier = Modifier.height(32.dp))
        HourlyImpactChartFromState(hours, hourlyImpactCounts)
    }
}

@Composable
fun GyroscopeChart(
    x: Float, y: Float, z: Float
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Gyroscope Readings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: $x", color = MaterialTheme.colorScheme.error)
        Text("Y: $y", color = MaterialTheme.colorScheme.secondary)
        Text("Z: $z", color = MaterialTheme.colorScheme.tertiary)
    }
}
