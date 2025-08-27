package com.example.practicaltrainingproject.presentation.ui.mainScreen.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


// Weekly falls chart using Canvas
@Preview
@Composable
fun FallHistoryBarChart() {
    // Sample data - falls per day (0-7)
    val fallData = mapOf(
        "Mon" to 3,
        "Tue" to 5,
        "Wed" to 2,
        "Thu" to 7,
        "Fri" to 4,
        "Sat" to 1,
        "Sun" to 0
    )

    val maxFalls = fallData.values.maxOrNull() ?: 10
    val barColor = Color(0xFF4285F4) // Google blue
    val textColor = Color.Black

    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(24.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bottomPadding = 40f // space for X labels
            val leftPadding = 40f   // space for Y labels
            val chartHeight = canvasHeight - bottomPadding
            val chartWidth = canvasWidth - leftPadding
            val barWidth = chartWidth / (fallData.size * 2)
            val dayLabelY = canvasHeight - 5f // draw just above the bottom edge

            val yScale = chartHeight * 0.8f / maxFalls

            // 🔹 Title inside the chart
            val chartTitle = "Weekly Fall History"
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

            // Y-axis lines and labels
            for (i in 0..maxFalls) {
                val yPos = chartHeight - (i * yScale)

                drawLine(
                    start = Offset(leftPadding, yPos),
                    end = Offset(canvasWidth, yPos),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        i.toString(),
                        5f, // aligned left
                        yPos + 8f, // vertical centering tweak
                        android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }
            }

            // Bars and labels
            fallData.entries.forEachIndexed { index, (day, falls) ->
                val barLeft = leftPadding + index * (barWidth * 2) + barWidth * 0.5f
                val barTop = chartHeight - (falls * yScale)
                val barBottom = chartHeight

                // Bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barBottom - barTop),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Fall count above bar
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        falls.toString(),
                        barLeft + barWidth / 2,
                        barTop - 10,
                        android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Day label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        day,
                        barLeft + barWidth / 2,
                        dayLabelY,
                        android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // Y-axis line
            drawLine(
                start = Offset(leftPadding, 0f),
                end = Offset(leftPadding, chartHeight),
                color = Color.Black,
                strokeWidth = 2.dp.toPx()
            )

            // X-axis line at zero level
            drawLine(
                start = Offset(leftPadding, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                color = Color.Black,
                strokeWidth = 2.dp.toPx()
            )
        }


    }
}