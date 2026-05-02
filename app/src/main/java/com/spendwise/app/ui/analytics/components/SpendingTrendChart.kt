package com.spendwise.app.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.analytics.ChartViewMode
import com.spendwise.app.ui.analytics.DailyTotal

@Composable
fun SpendingTrendChart(
    dailyTotals: List<DailyTotal>,
    viewMode: ChartViewMode,
    modifier: Modifier = Modifier
) {
    val data = when (viewMode) {
        ChartViewMode.DAILY -> dailyTotals
        ChartViewMode.WEEKLY -> {
            dailyTotals.chunked(7).mapIndexed { index, week ->
                DailyTotal(label = "W${index + 1}", amount = week.sumOf { it.amount })
            }
        }
    }

    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            if (data.isEmpty()) return@Canvas
            val barWidth = size.width / data.size * 0.7f
            val gap = size.width / data.size * 0.3f

            data.forEachIndexed { index, total ->
                val barHeight = (total.amount / maxAmount * size.height).toFloat()
                val x = index * (barWidth + gap) + gap / 2
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        // Labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.take(if (viewMode == ChartViewMode.WEEKLY) data.size else 10).forEach { item ->
                Text(item.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
