package com.example.practicaltrainingproject.presentation.ui.mainScreen.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.*
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat

// Extra data key for legend labels
private val LegendLabelKey = ExtraStore.Key<Set<String>>()

// Y-axis formatting (grams)
private val YDecimalFormat = DecimalFormat("#.## g")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step ({ 0.5 })

// Chart colors for each data series
private val ColumnColors = listOf(
    Color(0xFF6438A7),
    Color(0xFF3490DE),
    Color(0xFF73E8DC)
)

@Composable
private fun BarChartInternal(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier
) {
    val legendTextComponent = rememberTextComponent(vicoTheme.textColor)

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    ColumnColors.map { color ->
                        rememberLineComponent(
                            fill = fill(color),
                            thickness = 16.dp
                        )
                    }
                ),
                columnCollectionSpacing = 32.dp,
                mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = StartAxisValueFormatter,
                itemPlacer = StartAxisItemPlacer
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() }
            ),
            layerPadding = { cartesianLayerPadding(scalableStart = 16.dp, scalableEnd = 16.dp) },
            legend = rememberHorizontalLegend(
                items = { extraStore ->
                    extraStore[LegendLabelKey].forEachIndexed { index, label ->
                        add(
                            LegendItem(
                                shapeComponent(fill(ColumnColors[index]), CorneredShape.Pill),
                                legendTextComponent,
                                label
                            )
                        )
                    }
                },
                padding = insets(top = 16.dp)
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier.height(252.dp),
        zoomState = rememberVicoZoomState(zoomEnabled = false)
    )
}

// Example data for the past 7 days
private val xAxisLabels = (1..7).toList()
private val chartData = mapOf(
    "Calories" to listOf<Number>(2100, 2150, 2000, 2200, 2300, 1900, 2050)
)

@Composable
fun BarChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnSeries { chartData.values.forEach { series(xAxisLabels, it) } }
            extras { it[LegendLabelKey] = chartData.keys }
        }
    }

    BarChartInternal(modelProducer, modifier)
}
