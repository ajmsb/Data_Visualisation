package com.example.practicaltrainingproject.presentation.ui.sharedViewModels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practicaltrainingproject.data.manager.NotificationHelper
import com.example.practicaltrainingproject.data.manager.WhatsAppEmergencyHelper
import com.example.practicaltrainingproject.data.service.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FallEvent(
    val timestamp: String,
    val accelerationMagnitude: Double,
    val rotationMagnitude: Double,
    val wasCancelled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationString: String? = null
)

data class FallDetectionUiState(
    val isMonitoring: Boolean = false,
    val fallDetected: Boolean = false,
    val fallCount: Int = 0,
    val accelerometerData: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val gyroscopeData: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0),
    val fallHistory: List<FallEvent> = emptyList(),
    val countdownSeconds: Int = 0,
    val accelerationThreshold: Double = 5.0,
    val rotationThreshold: Double = 1.0,
    val sensorAccuracy: String = "Unknown",
    val lastFallTimestamp: String = "Never",
    val isLoading: Boolean = false,
    val error: String? = null
)

class FallDetectionViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FallDetectionUiState())
    val uiState: StateFlow<FallDetectionUiState> = _uiState.asStateFlow()

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Use singleton instance
    private val locationService = LocationService.getInstance(application.applicationContext)

    // Notification helper for instant alerts
    private val notificationHelper = NotificationHelper(application.applicationContext)

    // WhatsApp emergency helper
    private val whatsAppHelper = WhatsAppEmergencyHelper(application.applicationContext)

    private var countdownJob: Job? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private var lastAccelData = FloatArray(3)
    private var lastGyroData = FloatArray(3)

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            lastAccelData = event.values.clone()
            updateSensorData()
            checkForFall()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            val accuracyText = when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
                else -> "Unreliable"
            }
            _uiState.value = _uiState.value.copy(sensorAccuracy = accuracyText)
        }
    }

    private val gyroscopeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            lastGyroData = event.values.clone()
            updateSensorData()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun updateSensorData() {
        _uiState.value = _uiState.value.copy(
            accelerometerData = Triple(
                lastAccelData[0].toDouble(),
                lastAccelData[1].toDouble(),
                lastAccelData[2].toDouble()
            ),
            gyroscopeData = Triple(
                lastGyroData[0].toDouble(),
                lastGyroData[1].toDouble(),
                lastGyroData[2].toDouble()
            )
        )
    }

    private fun checkForFall() {
        if (!_uiState.value.isMonitoring || _uiState.value.fallDetected) return

        val accelMagnitude = kotlin.math.sqrt(
            lastAccelData[0] * lastAccelData[0] +
                    lastAccelData[1] * lastAccelData[1] +
                    lastAccelData[2] * lastAccelData[2]
        ).toDouble()

        val gyroMagnitude = kotlin.math.sqrt(
            lastGyroData[0] * lastGyroData[0] +
                    lastGyroData[1] * lastGyroData[1] +
                    lastGyroData[2] * lastGyroData[2]
        ).toDouble()

        // Remove gravity (9.8 m/s²) from acceleration
        val netAcceleration = kotlin.math.abs(accelMagnitude - 9.8)

        if (netAcceleration > _uiState.value.accelerationThreshold &&
            gyroMagnitude > _uiState.value.rotationThreshold
        ) {
            triggerFallDetection(netAcceleration, gyroMagnitude)
        }
    }

    private fun triggerFallDetection(accelMagnitude: Double, gyroMagnitude: Double) {
        _uiState.value = _uiState.value.copy(
            fallDetected = true,
            countdownSeconds = 10
        )

        // ⚡ SEND INSTANT NOTIFICATION - User gets alerted immediately!
        notificationHelper.sendFallDetectionNotification(
            acceleration = accelMagnitude,
            rotation = gyroMagnitude,
            countdownSeconds = 10
        )

        // Start countdown
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 10 downTo 1) {
                _uiState.value = _uiState.value.copy(countdownSeconds = i)

                // Update notification countdown every second
                notificationHelper.updateFallDetectionCountdown(i)

                delay(1000)
            }

            // If countdown completes without cancellation, confirm the fall
            if (_uiState.value.fallDetected) {
                confirmFall(accelMagnitude, gyroMagnitude, false)
            }
        }
    }

    fun cancelFallAlert() {
        countdownJob?.cancel()

        // Dismiss the fall detection notification
        notificationHelper.dismissFallDetectionNotification()

        val timestamp = dateFormat.format(Date())

        val cancelledEvent = FallEvent(
            timestamp = timestamp,
            accelerationMagnitude = 0.0,
            rotationMagnitude = 0.0,
            wasCancelled = true
        )

        _uiState.value = _uiState.value.copy(
            fallDetected = false,
            countdownSeconds = 0,
            fallHistory = _uiState.value.fallHistory + cancelledEvent
        )
    }

    private fun confirmFall(accelMagnitude: Double, gyroMagnitude: Double, isManual: Boolean) {
        val timestamp = dateFormat.format(Date())

        // Get current location
        val currentLocation = locationService.currentLocation.value
        val latitude = currentLocation?.latitude
        val longitude = currentLocation?.longitude
        val locationString = if (currentLocation != null) {
            "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}"
        } else {
            "Location unavailable"
        }

        val fallEvent = FallEvent(
            timestamp = timestamp,
            accelerationMagnitude = accelMagnitude,
            rotationMagnitude = gyroMagnitude,
            wasCancelled = false,
            latitude = latitude,
            longitude = longitude,
            locationString = locationString
        )

        _uiState.value = _uiState.value.copy(
            fallCount = _uiState.value.fallCount + 1,
            fallHistory = _uiState.value.fallHistory + fallEvent,
            lastFallTimestamp = timestamp,
            countdownSeconds = 0,
            fallDetected = false
        )

        // Dismiss countdown notification
        notificationHelper.dismissFallDetectionNotification()

        // Trigger emergency contact notification with location and acceleration
        triggerEmergencyContact(
            accelMagnitude = accelMagnitude,
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun triggerEmergencyContact(
        accelMagnitude: Double,
        latitude: Double?,
        longitude: Double?
    ) {
        // Send emergency confirmed notification
        notificationHelper.sendEmergencyConfirmedNotification(null)

        // Send broadcast to trigger emergency actions
        val context = getApplication<Application>().applicationContext
        val intent = android.content.Intent("FALL_CONFIRMED")
        context.sendBroadcast(intent)

        // TODO: Send WhatsApp message - needs user data from AuthViewModel
        // This will be called from MainActivity/MainScreen where user data is available
        // Store the fall data for WhatsApp sending
        storeFallDataForEmergency(accelMagnitude, latitude, longitude)
    }

    // Store latest fall data for emergency contact
    private var latestFallData: Triple<Double, Double?, Double?>? = null

    private fun storeFallDataForEmergency(
        accelMagnitude: Double,
        latitude: Double?,
        longitude: Double?
    ) {
        latestFallData = Triple(accelMagnitude, latitude, longitude)
    }

    /**
     * Send WhatsApp emergency alert with user information
     * Call this from UI layer where user data is available
     */
    fun sendWhatsAppEmergencyAlert(
        userName: String,
        emergencyContact: String?,
        emergencyContacts: List<String> = emptyList(),
        autoSmsEnabled: Boolean = true
    ) {
        latestFallData?.let { (acceleration, latitude, longitude) ->
            val location = if (latitude != null && longitude != null) {
                Pair(latitude, longitude)
            } else null

            // Get all contacts to notify
            val allContacts = buildList {
                if (!emergencyContact.isNullOrBlank() && emergencyContact !in emergencyContacts) {
                    add(emergencyContact)
                }
                addAll(emergencyContacts)
            }.distinct()

            if (allContacts.isEmpty()) {
                android.util.Log.w("FallDetectionViewModel", "No emergency contacts to notify")
                return
            }

            // Send to all contacts simultaneously
            whatsAppHelper.sendEmergencyAlertsToAllContacts(
                userName = userName,
                emergencyContacts = allContacts,
                location = location,
                acceleration = acceleration,
                autoSmsEnabled = autoSmsEnabled
            )
        }
    }

    fun dismissFallAlert() {
        _uiState.value = _uiState.value.copy(
            fallDetected = false,
            countdownSeconds = 0
        )
    }

    fun manualEmergency() {
        if (!_uiState.value.fallDetected) {
            confirmFall(0.0, 0.0, true)
            _uiState.value = _uiState.value.copy(fallDetected = true)
        }
    }

    fun toggleMonitoring() {
        val newState = !_uiState.value.isMonitoring

        if (newState) {
            startMonitoring()
        } else {
            stopMonitoring()
        }

        _uiState.value = _uiState.value.copy(
            isMonitoring = newState,
            fallDetected = false,
            countdownSeconds = 0
        )
    }

    private fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        gyroscope?.let {
            sensorManager.registerListener(
                gyroscopeListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(accelerometerListener)
        sensorManager.unregisterListener(gyroscopeListener)
        countdownJob?.cancel()
    }

    fun updateAccelerationThreshold(value: Double) {
        _uiState.value = _uiState.value.copy(accelerationThreshold = value)
    }

    fun updateRotationThreshold(value: Double) {
        _uiState.value = _uiState.value.copy(rotationThreshold = value)
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(
            fallHistory = emptyList(),
            fallCount = 0,
            lastFallTimestamp = "Never"
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}