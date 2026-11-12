package com.example.practicaltrainingproject.data.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.practicaltrainingproject.data.auth.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper class to manage emergency contacts via SMS and WhatsApp
 * SMS is sent automatically, WhatsApp is opened as backup
 */
class WhatsAppEmergencyHelper(private val context: Context) {

    /**
     * Send emergency fall alert via SMS (automatic) and WhatsApp (manual)
     */
    fun sendEmergencyAlert(
        phoneNumber: String,
        userName: String,
        location: Pair<Double, Double>?, // latitude, longitude
        acceleration: Double? = null,
        timestamp: Long = System.currentTimeMillis(),
        autoSmsEnabled: Boolean = true
    ): Boolean {
        return try {
            // Format phone number
            val formattedNumber = formatPhoneNumber(phoneNumber)

            // 1. SEND SMS AUTOMATICALLY (Primary method) - Only if enabled
            val smsSuccess = if (autoSmsEnabled) {
                sendSMSEmergencyAlert(
                    phoneNumber = phoneNumber,
                    userName = userName,
                    location = location,
                    acceleration = acceleration,
                    timestamp = timestamp
                )
            } else {
                Log.d(TAG, "Auto Emergency SMS disabled in settings. Skipping SMS.")
                Toast.makeText(
                    context,
                    "Auto SMS disabled. Use WhatsApp to send alert.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            // 2. OPEN WHATSAPP AS BACKUP (Secondary method)
            if (isWhatsAppAvailable()) {
                // Create emergency message for WhatsApp
                val message = buildEmergencyMessage(
                    userName = userName,
                    location = location,
                    acceleration = acceleration,
                    timestamp = timestamp
                )

                // Open WhatsApp with pre-filled message (user can send if needed)
                sendWhatsAppMessage(formattedNumber, message)
            }

            smsSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to send emergency alert: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Send SMS emergency alert automatically (no user interaction needed)
     */
    private fun sendSMSEmergencyAlert(
        phoneNumber: String,
        userName: String,
        location: Pair<Double, Double>?,
        acceleration: Double?,
        timestamp: Long
    ): Boolean {
        return try {
            // Check SMS permission first
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "SMS permission not granted!")
                Toast.makeText(
                    context,
                    "SMS permission not granted. Please enable in app settings.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }

            Log.d(TAG, "SMS permission granted. Attempting to send SMS to: $phoneNumber")

            val dateTime = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date(timestamp))

            // Build SMS message (shorter, no formatting)
            val message = buildString {
                append("🚨 EMERGENCY FALL ALERT 🚨\n\n")
                append("User: $userName\n")
                append("Time: $dateTime\n")

                if (acceleration != null) {
                    val severity = when {
                        acceleration > 35.0 -> "CRITICAL"
                        acceleration > 28.0 -> "HIGH"
                        acceleration > 25.0 -> "MODERATE"
                        else -> "LOW"
                    }
                    append("Impact: ${"%.1f".format(acceleration)} m/s² ($severity)\n")
                }

                if (location != null) {
                    append("\nLocation:\n")
                    append("https://maps.google.com/?q=${location.first},${location.second}\n")
                }

                append("\n⚠️ Please check on $userName IMMEDIATELY!")
            }

            Log.d(TAG, "SMS message prepared. Length: ${message.length}")

            // Send SMS using SmsManager
            val smsManager = context.getSystemService(SmsManager::class.java)

            if (smsManager == null) {
                Log.e(TAG, "SmsManager is null!")
                Toast.makeText(context, "SMS service not available", Toast.LENGTH_LONG).show()
                return false
            }

            Log.d(TAG, "SmsManager obtained. Sending SMS...")

            // If message is too long, split it
            if (message.length > 160) {
                Log.d(TAG, "Message is long. Splitting into parts...")
                val parts = smsManager.divideMessage(message)
                Log.d(TAG, "Message split into ${parts.size} parts")
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                Log.d(TAG, "Sending single SMS...")
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }

            Log.d(TAG, "SMS sent successfully!")

            // Show confirmation toast
            Toast.makeText(
                context,
                "Emergency SMS sent to $phoneNumber",
                Toast.LENGTH_LONG
            ).show()

            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while sending SMS: ${e.message}", e)
            Toast.makeText(
                context,
                "SMS permission denied: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        } catch (e: Exception) {
            Log.e(TAG, "Exception while sending SMS: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(
                context,
                "SMS failed: ${e.message}. Check WhatsApp instead.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    /**
     * Send emergency alert to emergency contact
     */
    fun sendEmergencyAlertToContact(
        user: User,
        location: Pair<Double, Double>?,
        acceleration: Double?
    ): Boolean {
        return user.emergencyContact?.let { contact ->
            sendEmergencyAlert(
                phoneNumber = contact,
                userName = user.name,
                location = location,
                acceleration = acceleration
            )
        } ?: false
    }

    /**
     * Send emergency alerts to ALL emergency contacts simultaneously
     */
    fun sendEmergencyAlertsToAllContacts(
        userName: String,
        emergencyContacts: List<String>,
        location: Pair<Double, Double>?,
        acceleration: Double?,
        autoSmsEnabled: Boolean = true
    ): Int {
        var successCount = 0

        Log.d(TAG, "Sending emergency alerts to ${emergencyContacts.size} contacts")

        emergencyContacts.forEach { contact ->
            if (contact.isNotBlank()) {
                try {
                    val success = sendEmergencyAlert(
                        phoneNumber = contact,
                        userName = userName,
                        location = location,
                        acceleration = acceleration,
                        autoSmsEnabled = autoSmsEnabled
                    )

                    if (success) {
                        successCount++
                        Log.d(TAG, "Successfully sent alert to: $contact")
                    } else {
                        Log.e(TAG, "Failed to send alert to: $contact")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception sending alert to $contact: ${e.message}", e)
                }
            }
        }

        Log.d(
            TAG,
            "Emergency alerts sent to $successCount out of ${emergencyContacts.size} contacts"
        )

        // Show summary toast
        if (successCount > 0) {
            Toast.makeText(
                context,
                "Emergency alerts sent to $successCount contact(s)",
                Toast.LENGTH_LONG
            ).show()
        }

        return successCount
    }

    /**
     * Format phone number for WhatsApp (international format)
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except +
        var formatted = phoneNumber.replace(Regex("[^0-9+]"), "")

        // Remove + at the beginning for WhatsApp API
        if (formatted.startsWith("+")) {
            formatted = formatted.substring(1)
        }

        return formatted
    }

    /**
     * Build emergency message text with formatting
     */
    private fun buildEmergencyMessage(
        userName: String,
        location: Pair<Double, Double>?,
        acceleration: Double?,
        timestamp: Long
    ): String {
        val dateTime = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date(timestamp))

        return buildString {
            appendLine("🚨 *EMERGENCY FALL ALERT* 🚨")
            appendLine()
            appendLine("*User:* $userName")
            appendLine("*Time:* $dateTime")
            appendLine()

            if (acceleration != null) {
                val severity = when {
                    acceleration > 35.0 -> "Critical"
                    acceleration > 28.0 -> "High"
                    acceleration > 25.0 -> "Moderate"
                    else -> "Low"
                }
                appendLine("*Impact Force:* ${"%.1f".format(acceleration)} m/s²")
                appendLine("*Severity:* $severity")
                appendLine()
            }

            if (location != null) {
                appendLine("*Location:*")
                appendLine("📍 Lat: ${"%.6f".format(location.first)}")
                appendLine("📍 Lng: ${"%.6f".format(location.second)}")
                appendLine()
                appendLine("*Open in Google Maps:*")
                appendLine("https://maps.google.com/?q=${location.first},${location.second}")
                appendLine()
            } else {
                appendLine("*Location:* Not available")
                appendLine()
            }

            appendLine("⚠️ *Please check on $userName immediately!*")
            appendLine()
            appendLine("This is an automated emergency alert from Fall Detection App")
        }
    }

    /**
     * Send message via WhatsApp
     */
    private fun sendWhatsAppMessage(phoneNumber: String, message: String) {
        try {
            // Try WhatsApp Business first (if installed)
            val whatsappBusinessIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://api.whatsapp.com/send?phone=$phoneNumber&text=${
                        Uri.encode(message)
                    }"
                )
                setPackage("com.whatsapp.w4b")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (isWhatsAppBusinessInstalled()) {
                context.startActivity(whatsappBusinessIntent)
                return
            }
        } catch (e: Exception) {
            // Business version failed, try regular WhatsApp
        }

        try {
            // Try regular WhatsApp
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://api.whatsapp.com/send?phone=$phoneNumber&text=${
                        Uri.encode(message)
                    }"
                )
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (isWhatsAppInstalled()) {
                context.startActivity(whatsappIntent)
                return
            }
        } catch (e: Exception) {
            // Regular WhatsApp failed
        }

        // If WhatsApp is not installed, open in browser (WhatsApp Web)
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://api.whatsapp.com/send?phone=$phoneNumber&text=${
                    Uri.encode(message)
                }"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    }

    /**
     * Check if WhatsApp is installed
     */
    private fun isWhatsAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if WhatsApp Business is installed
     */
    private fun isWhatsAppBusinessInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if any WhatsApp version is available
     */
    fun isWhatsAppAvailable(): Boolean {
        return isWhatsAppInstalled() || isWhatsAppBusinessInstalled()
    }

    /**
     * Check if SMS permission is granted
     */
    fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get WhatsApp availability status message
     */
    fun getWhatsAppStatusMessage(): String {
        return when {
            isWhatsAppBusinessInstalled() -> "WhatsApp Business available"
            isWhatsAppInstalled() -> "WhatsApp available"
            else -> "WhatsApp not installed - will open in browser"
        }
    }

    companion object {
        const val TAG = "WhatsAppEmergencyHelper"
    }
}

