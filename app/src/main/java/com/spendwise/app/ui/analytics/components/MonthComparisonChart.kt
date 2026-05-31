package com.spendwise.app.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.usecase.CategoryComparison
import com.spendwise.app.domain.usecase.MonthComparison
import java.text.NumberFormat
import java.util.Locale
import com.spendwise.app.ui.components.MatteCard

@Composable
fun MonthComparisonChart(
    comparison: MonthComparison,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Total comparison
        MatteCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Spending", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(comparison.periodA.label, style = MaterialTheme.typography.bodySmall, color = primaryColor)
                        Text(currencyFormat.format(comparison.totalA), style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(comparison.periodB.label, style = MaterialTheme.typography.bodySmall, color = secondaryColor)
                        Text(currencyFormat.format(comparison.totalB), style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                val deltaText = if (comparison.deltaPercentage >= 0) "+${comparison.deltaPercentage.toInt()}%" else "${comparison.deltaPercentage.toInt()}%"
                val deltaColor = if (comparison.deltaPercentage > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                Text("Change: $deltaText", style = MaterialTheme.typography.titleSmall, color = deltaColor)
            }
        }

        // Category comparison bars
        MatteCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("By Category", style = MaterialTheme.typography.titleMedium)

                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(Modifier.size(12.dp)) { drawCircle(primaryColor) }
                        Spacer(Modifier.width(4.dp))
                        Text(comparison.periodA.label, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(Modifier.size(12.dp)) { drawCircle(secondaryColor) }
                        Spacer(Modifier.width(4.dp))
                        Text(comparison.periodB.label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                val maxAmount = comparison.categoryComparisons.maxOfOrNull { maxOf(it.amountA, it.amountB) } ?: 1.0

                comparison.categoryComparisons.take(8).forEach { cat ->
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cat.categoryName, style = MaterialTheme.typography.bodyMedium)
                            val delta = if (cat.deltaPercentage >= 0) "+${cat.deltaPercentage.toInt()}%" else "${cat.deltaPercentage.toInt()}%"
                            Text(delta, style = MaterialTheme.typography.bodySmall,
                                color = if (cat.deltaPercentage > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        // Side-by-side bars
                        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                            val barHeight = size.height / 2 - 2.dp.toPx()
                            val widthA = (cat.amountA / maxAmount * size.width).toFloat()
                            val widthB = (cat.amountB / maxAmount * size.width).toFloat()

                            drawRect(primaryColor, Offset.Zero, Size(widthA, barHeight))
                            drawRect(secondaryColor, Offset(0f, barHeight + 4.dp.toPx()), Size(widthB, barHeight))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currencyFormat.format(cat.amountA), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(cat.amountB), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
