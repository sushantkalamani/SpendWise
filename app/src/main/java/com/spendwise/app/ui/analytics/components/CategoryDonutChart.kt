package com.spendwise.app.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.ui.theme.CategoryColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoryDonutChart(
    breakdown: List<CategorySpend>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 32.dp.toPx()
                var startAngle = -90f
                breakdown.forEachIndexed { index, spend ->
                    val sweep = (spend.percentage / 100 * 360).toFloat()
                    val color = try {
                        Color(android.graphics.Color.parseColor(spend.category.colorHex))
                    } catch (_: Exception) {
                        CategoryColors[index % CategoryColors.size]
                    }
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currencyFormat.format(totalAmount), style = MaterialTheme.typography.titleMedium)
                Text("total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            breakdown.take(6).forEachIndexed { index, spend ->
                val color = try {
                    Color(android.graphics.Color.parseColor(spend.category.colorHex))
                } catch (_: Exception) {
                    CategoryColors[index % CategoryColors.size]
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Canvas(Modifier.size(12.dp)) { drawCircle(color) }
                    Spacer(Modifier.width(8.dp))
                    Text(spend.category.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("${spend.percentage.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Text(currencyFormat.format(spend.amount), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
