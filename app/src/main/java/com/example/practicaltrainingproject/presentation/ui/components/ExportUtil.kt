package com.example.practicaltrainingproject.presentation.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.practicaltrainingproject.presentation.ui.sharedViewModels.FallEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting fall history data
 */
object ExportUtil {

    /**
     * Export fall history to CSV file
     */
    fun exportFallHistoryToCSV(
        context: Context,
        fallHistory: List<FallEvent>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            // Create CSV content
            val csv = buildString {
                append("Timestamp,Acceleration (m/s²),Rotation (rad/s),Status,Latitude,Longitude,Location\n")

                fallHistory.forEach { fall ->
                    append("${fall.timestamp},")
                    append("${"%.2f".format(fall.accelerationMagnitude)},")
                    append("${"%.2f".format(fall.rotationMagnitude)},")
                    append(if (fall.wasCancelled) "Cancelled" else "Confirmed")
                    append(",")
                    append(if (fall.latitude != null) "%.6f".format(fall.latitude) else "N/A")
                    append(",")
                    append(if (fall.longitude != null) "%.6f".format(fall.longitude) else "N/A")
                    append(",")
                    append("\"${fall.locationString ?: "Location unavailable"}\"")
                    append("\n")
                }
            }

            // Create file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "fall_history_$timestamp.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(csv)

            // Share file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Fall Detection History")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Fall detection history exported on ${
                        SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.getDefault()
                        ).format(Date())
                    }"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Fall History"))
            onSuccess()

        } catch (e: Exception) {
            onError(e.message ?: "Failed to export data")
        }
    }

    /**
     * Export to PDF format (simplified version)
     */
    fun exportFallHistoryToPDF(
        context: Context,
        fallHistory: List<FallEvent>,
        userName: String = "User",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // For now, export as formatted text that can be converted to PDF
        try {
            val content = buildString {
                append("=== FALL DETECTION REPORT ===\n\n")
                append("User: $userName\n")
                append(
                    "Generated: ${
                        SimpleDateFormat(
                            "MMM dd, yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date())
                    }\n"
                )
                append("Total Events: ${fallHistory.size}\n")
                append("Confirmed Falls: ${fallHistory.count { !it.wasCancelled }}\n")
                append("Cancelled Alerts: ${fallHistory.count { it.wasCancelled }}\n\n")
                append("=== EVENT HISTORY ===\n\n")

                fallHistory.reversed().forEach { fall ->
                    append("${if (fall.wasCancelled) "⚠️" else "🚨"} ${fall.timestamp}\n")
                    append("  Status: ${if (fall.wasCancelled) "Cancelled" else "Confirmed"}\n")
                    if (!fall.wasCancelled) {
                        append("  Acceleration: ${"%.2f".format(fall.accelerationMagnitude)} m/s²\n")
                        append("  Rotation: ${"%.2f".format(fall.rotationMagnitude)} rad/s\n")
                    }
                    if (fall.latitude != null && fall.longitude != null) {
                        append("  Location: ${fall.locationString}\n")
                        append("  Map: https://maps.google.com/?q=${fall.latitude},${fall.longitude}\n")
                    } else {
                        append("  Location: Not available\n")
                    }
                    append("\n")
                }
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "fall_report_$timestamp.txt"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Fall Detection Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Fall Report"))
            onSuccess()

        } catch (e: Exception) {
            onError(e.message ?: "Failed to export report")
        }
    }
}

