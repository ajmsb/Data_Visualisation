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
import androidx.compose.ui.unit.dp

@Composable
fun WeeklyImpactChartFromState(days: List<String>, dailyImpactCounts: List<Int>) {
    val maxCount = (dailyImpactCounts.maxOrNull() ?: 1).coerceAtLeast(1)
    val barColor = Color(0xFFF44266)
    val textColor = Color.Black

    Box(
        modifier = Modifier
            .padding(10.dp)
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
            val chartHeight = canvasHeight - bottomPadding
            val chartWidth = canvasWidth
            val barWidth = chartWidth / (days.size * 2)
            val dayLabelY = canvasHeight - 5f

            val yScale = chartHeight * 0.8f / maxCount

            // Title
            val chartTitle = "Weekly Impact History"
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
//            for (i in 0..maxCount) {
//                val yPos = chartHeight - (i * yScale)
//                drawLine(
//                    start = Offset(leftPadding, yPos),
//                    end = Offset(canvasWidth, yPos),
//                    color = Color.LightGray.copy(alpha = 0.3f),
//                    strokeWidth = 1.dp.toPx()
//                )
//                drawContext.canvas.nativeCanvas.drawText(
//                    i.toString(),
//                    5f,
//                    yPos + 8f,
//                    android.graphics.Paint().apply {
//                        color = textColor.toArgb()
//                        textSize = 24f
//                        textAlign = android.graphics.Paint.Align.LEFT
//                    }
//                )
//            }

            // Bars and labels
            days.forEachIndexed { index, day ->
                val falls = dailyImpactCounts.getOrNull(index) ?: 0
                val barLeft =  index * (barWidth * 2) + barWidth * 0.5f
                val barTop = chartHeight - (falls * yScale)
                val barBottom = chartHeight

                // Bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barBottom - barTop),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Count above bar
                drawContext.canvas.nativeCanvas.drawText(
                    falls.toString(),
                    barLeft + barWidth / 2,
                    barTop - 10,
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                // Day label
                drawContext.canvas.nativeCanvas.drawText(
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

            // Axes
//            drawLine(
//                start = Offset(leftPadding, 0f),
//                end = Offset(leftPadding, chartHeight),
//                color = Color.Black,
//                strokeWidth = 2.dp.toPx()
//            )
            drawLine(
                start = Offset(0f, chartHeight),
                end = Offset(canvasWidth, chartHeight),
                color = Color.Black,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}