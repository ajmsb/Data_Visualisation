package com.example.practicaltrainingproject.data.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Helper class to manage notification channels and settings
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_FALL_DETECTION = "fall_detection_channel"
        const val CHANNEL_ID_EMERGENCY = "emergency_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"

        private const val CHANNEL_NAME_FALL_DETECTION = "Fall Detection"
        private const val CHANNEL_NAME_EMERGENCY = "Emergency Alerts"
        private const val CHANNEL_NAME_GENERAL = "General Notifications"

        private const val FALL_DETECTION_NOTIFICATION_ID = 1001
        private const val EMERGENCY_NOTIFICATION_ID = 1002
    }

    /**
     * Create all notification channels
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Fall Detection Channel (High Priority)
            val fallDetectionChannel = NotificationChannel(
                CHANNEL_ID_FALL_DETECTION,
                CHANNEL_NAME_FALL_DETECTION,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for fall detection alerts and monitoring status"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // Emergency Channel (Max Priority)
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID_EMERGENCY,
                CHANNEL_NAME_EMERGENCY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical emergency alerts for confirmed falls"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // General Channel (Default Priority)
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications and updates"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // Create all channels
            notificationManager.createNotificationChannel(fallDetectionChannel)
            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    /**
     * Check if notifications are enabled for the app
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Check if a specific channel is enabled
     */
    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return areNotificationsEnabled()
    }

    /**
     * Open notification settings for the app
     */
    fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .apply {
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
        } else {
            android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
        }
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Send a test notification
     */
    fun sendTestNotification() {
        // Only send if notifications are enabled
        if (!areNotificationsEnabled()) return

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Notifications Enabled")
                .setContentText("You will now receive fall detection alerts and updates")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: SecurityException) {
            // Permission was denied - ignore
        }
    }

    /**
     * Send immediate fall detection alert notification
     */
    fun sendFallDetectionNotification(
        acceleration: Double,
        rotation: Double,
        countdownSeconds: Int = 10
    ) {
        // Only send if notifications are enabled
        if (!areNotificationsEnabled()) return

        try {
            // Create intent to open the app
            val intent = android.content.Intent(
                context,
                Class.forName("com.example.practicaltrainingproject.MainActivity")
            ).apply {
                flags =
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_FALL_DETECTION)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ FALL DETECTED!")
                .setContentText("Impact: ${"%.1f".format(acceleration)} m/s² - Countdown: ${countdownSeconds}s")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Fall detected! Tap to cancel.\n\nImpact: ${
                                "%.1f".format(
                                    acceleration
                                )
                            } m/s²\nRotation: ${"%.1f".format(rotation)} rad/s\n\nEmergency contacts will be notified in ${countdownSeconds} seconds unless cancelled."
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .build()

            try {
                NotificationManagerCompat.from(context)
                    .notify(FALL_DETECTION_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Permission denied
            }
        } catch (_: Exception) {
            // Failed to send notification - log or ignore
        }
    }

    /**
     * Update fall detection notification with new countdown
     */
    fun updateFallDetectionCountdown(countdownSeconds: Int) {
        if (!areNotificationsEnabled()) return

        try {
            val intent = android.content.Intent(
                context,
                Class.forName("com.example.practicaltrainingproject.MainActivity")
            ).apply {
                flags =
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_FALL_DETECTION)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ FALL DETECTED!")
                .setContentText("Tap to cancel - ${countdownSeconds}s remaining")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Fall detected! Tap to cancel.\n\nEmergency contacts will be notified in ${countdownSeconds} seconds unless you cancel.")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context)
                    .notify(FALL_DETECTION_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Permission denied
            }
        } catch (_: Exception) {
            // Failed to update notification
        }
    }

    /**
     * Cancel/dismiss fall detection notification
     */
    fun dismissFallDetectionNotification() {
        try {
            NotificationManagerCompat.from(context).cancel(FALL_DETECTION_NOTIFICATION_ID)
        } catch (_: SecurityException) {
            // Failed to dismiss notification
        }
    }

    /**
     * Send emergency confirmed notification
     */
    fun sendEmergencyConfirmedNotification(contactName: String?) {
        if (!areNotificationsEnabled()) return

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_EMERGENCY)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 EMERGENCY ALERT SENT")
                .setContentText("Emergency contacts have been notified")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Fall confirmed! Emergency contacts have been notified.\n\n${contactName?.let { "Contact: $it" } ?: "Contact: Not set"}"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .build()

            try {
                NotificationManagerCompat.from(context)
                    .notify(EMERGENCY_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Permission denied
            }
        } catch (_: Exception) {
            // Failed to send notification
        }
    }


    /**
     * Get notification channel status
     */
    fun getChannelStatus(): Map<String, Boolean> {
        return mapOf(
            "appEnabled" to areNotificationsEnabled(),
            "fallDetection" to isChannelEnabled(CHANNEL_ID_FALL_DETECTION),
            "emergency" to isChannelEnabled(CHANNEL_ID_EMERGENCY),
            "general" to isChannelEnabled(CHANNEL_ID_GENERAL)
        )
    }
}

