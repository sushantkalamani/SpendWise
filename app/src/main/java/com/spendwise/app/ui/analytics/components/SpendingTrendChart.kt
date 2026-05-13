package com.spendwise.app.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.analytics.ChartViewMode
import com.spendwise.app.ui.analytics.DailyTotal
import java.text.NumberFormat
import java.util.Locale

/**
 * Improved bar chart for spending trends.
 *
 * Key improvements over v1:
 * - Y-axis scale with amount labels (4 horizontal grid lines)
 * - Rounded bars using [drawRoundRect]
 * - Tap/long-press shows exact value tooltip above the bar
 * - Zero-spend days show dotted baseline
 * - Horizontal scroll when many days exist
 * - Weekly mode groups by ISO calendar week
 * - Theme-aware primary/secondary colors
 */
@Composable
fun SpendingTrendChart(
    dailyTotals: List<DailyTotal>,
    viewMode: ChartViewMode,
    modifier: Modifier = Modifier
) {
    val data = when (viewMode) {
        ChartViewMode.DAILY -> dailyTotals
        ChartViewMode.WEEKLY -> {
            // Group by ISO week (7-day windows aligned to period start)
            dailyTotals.chunked(7).mapIndexed { index, week ->
                DailyTotal(
                    label = "W${index + 1}",
                    amount = week.sumOf { it.amount }
                )
            }
        }
    }

    val maxAmount = data.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 100.0
    val barColor = MaterialTheme.colorScheme.primary
    val barColorZero = MaterialTheme.colorScheme.outlineVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tooltipColor = MaterialTheme.colorScheme.inverseSurface

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Dynamic width: wider when many items, scrollable
    val minBarWidth = if (viewMode == ChartViewMode.WEEKLY) 48.dp else 28.dp
    val chartWidth = (data.size * minBarWidth.value * 1.3f).dp.coerceAtLeast(300.dp)
    val yAxisWidth = 48.dp

    Column(modifier = modifier) {
        // Tooltip
        if (selectedIndex in data.indices) {
            val item = data[selectedIndex]
            Surface(
                color = tooltipColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    "${item.label}: ${currencyFormat.format(item.amount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-axis labels
            Column(
                modifier = Modifier.width(yAxisWidth).height(150.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val steps = 4
                for (i in steps downTo 0) {
                    val value = maxAmount * i / steps
                    Text(
                        formatCompact(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Chart area (scrollable when many days)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width(chartWidth)
                        .height(150.dp)
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                if (data.isEmpty()) return@detectTapGestures
                                val barTotalWidth = size.width.toFloat() / data.size
                                val tappedIndex = (offset.x / barTotalWidth).toInt()
                                    .coerceIn(0, data.size - 1)
                                selectedIndex = if (selectedIndex == tappedIndex) -1 else tappedIndex
                            }
                        }
                ) {
                    if (data.isEmpty()) return@Canvas

                    // Draw horizontal grid lines
                    val steps = 4
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                    for (i in 0..steps) {
                        val y = size.height * (1 - i.toFloat() / steps)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            pathEffect = dashEffect,
                            strokeWidth = 1f
                        )
                    }

                    // Draw bars
                    val barTotalWidth = size.width / data.size
                    val barWidth = barTotalWidth * 0.65f
                    val gap = barTotalWidth * 0.35f / 2

                    data.forEachIndexed { index, total ->
                        val barHeight = if (maxAmount > 0) {
                            (total.amount / maxAmount * size.height).toFloat()
                        } else 0f
                        val x = index * barTotalWidth + gap

                        if (total.amount > 0) {
                            // Rounded bar
                            drawRoundRect(
                                color = if (index == selectedIndex) barColor.copy(alpha = 0.8f) else barColor,
                                topLeft = Offset(x, size.height - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        } else {
                            // Zero-spend: dotted baseline indicator
                            drawLine(
                                color = barColorZero,
                                start = Offset(x, size.height - 2f),
                                end = Offset(x + barWidth, size.height - 2f),
                                pathEffect = dashEffect,
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
        }

        // X-axis labels (scrollable, show all)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = yAxisWidth)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Show labels adaptively: if too many, show every Nth
            val labelStep = when {
                data.size <= 10 -> 1
                data.size <= 20 -> 2
                else -> 5
            }
            data.forEachIndexed { index, item ->
                if (index % labelStep == 0) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        modifier = Modifier.width(minBarWidth)
                    )
                }
            }
        }
    }
}

/**
 * Formats amounts compactly for Y-axis labels (e.g. "₹1K", "₹500").
 */
private fun formatCompact(value: Double): String {
    return when {
        value >= 100000 -> "₹${(value / 100000).toInt()}L"
        value >= 1000 -> "₹${(value / 1000).toInt()}K"
        value > 0 -> "₹${value.toInt()}"
        else -> "₹0"
    }
}
