package com.example.practicaltrainingproject.presentation.ui.mainScreen.components

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun HourlyImpactChartFromState(hours: List<String>, hourlyImpactCounts: List<Int>) {
    val maxCount = (hourlyImpactCounts.maxOrNull() ?: 1).coerceAtLeast(1)
    val lineColor = Color(0xFF4287F4)
    val dotColor = Color(0xFFF44266)
    val textColor = Color.Black

    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 40f // space for X labels
            val leftPadding = 40f // space for Y labels
            val chartHeight = canvasHeight - bottomPadding
            val chartWidth = canvasWidth - leftPadding
            // Title
            val chartTitle = "Hourly Impact History"
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

            // Draw Y axis labels (0, max)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "0",
                    leftPadding - 30f,
                    chartHeight,
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 24f
                    }
                )
                drawText(
                    "$maxCount",
                    leftPadding - 35f,
                    25f,
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 24f
                    }
                )
            }

            // Draw X axis labels
            val hourCount = hours.size
            val stepX = chartWidth / (hourCount - 1).coerceAtLeast(1)
            for (i in hours.indices) {
                val x = leftPadding + i * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    hours[i],
                    x - 10f,
                    chartHeight + 30f,
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 20f
                    }
                )
            }

            // Draw line chart
            val path = Path()
            for (i in hourlyImpactCounts.indices) {
                val x = leftPadding + i * stepX
                val y = chartHeight - (hourlyImpactCounts[i].toFloat() / maxCount) * (chartHeight - 20f)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw dots at each hour
            for (i in hourlyImpactCounts.indices) {
                val x = leftPadding + i * stepX
                val y = chartHeight - (hourlyImpactCounts[i].toFloat() / maxCount) * (chartHeight - 20f)
                drawCircle(
                    color = dotColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
