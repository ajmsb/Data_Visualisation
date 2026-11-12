/**
 * Background service for continuous fall monitoring
 * Runs even when app is in background or device is locked
 */
package com.example.practicaltrainingproject.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.example.practicaltrainingproject.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "FallDetectionChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        startForeground(NOTIFICATION_ID, createNotification())

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Start background monitoring logic
        monitoringJob = scope.launch {
            // Background monitoring logic would go here
            // This would integrate with your existing fall detection algorithm
        }
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        monitoringJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            // Process sensor data for fall detection
            // This would use the same logic as your FallDetectionViewModel
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                checkForFall(event.values[0], event.values[1], event.values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun checkForFall(x: Float, y: Float, z: Float) {
        val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
        val netAcceleration = Math.abs(magnitude - 9.8)

        // Fall detection threshold (same as in your ViewModel)
        if (netAcceleration > 25.0) {
            triggerFallAlert()
        }
    }

    private fun triggerFallAlert() {
        // Send broadcast to app
        val intent = Intent("FALL_DETECTED_IN_BACKGROUND")
        sendBroadcast(intent)

        // Trigger emergency contact notification
        triggerEmergencyContact()
    }

    private fun triggerEmergencyContact() {
        // Get emergency contact from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val emergencyContact = prefs.getString("emergency_contact", null)
        val userName = prefs.getString("user_name", "User")

        emergencyContact?.let { contact ->
            // Send SMS
            try {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                val message =
                    "🚨 FALL ALERT: $userName may have fallen and needs assistance. Please check on them immediately!"
                smsManager.sendTextMessage(contact, null, message, null, null)

                // Show notification that SMS was sent
                showEmergencyNotification("Emergency SMS sent to $contact")
            } catch (_: Exception) {
                // SMS failed - show notification to call manually
                showEmergencyNotification("Fall detected! Tap to call emergency contact", contact)
            }
        }
    }

    private fun showEmergencyNotification(message: String, phoneNumber: String? = null) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Create intent for calling if phone number provided
        val intent = phoneNumber?.let {
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$it")
            }
        } ?: Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Fall Detected!")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detection Active")
            .setContentText("Monitoring for falls in background")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fall Detection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background fall detection monitoring"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
}