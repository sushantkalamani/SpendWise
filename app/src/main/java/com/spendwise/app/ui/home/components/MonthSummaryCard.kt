package com.spendwise.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MonthSummaryCard(
    totalSpent: Double,
    totalBudget: Double?,
    daysRemaining: Int,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val ratio = if (totalBudget != null && totalBudget > 0) {
        (totalSpent / totalBudget).toFloat()
    } else {
        0f
    }
    val progressColor = when {
        ratio >= 0.9f -> MaterialTheme.colorScheme.error
        ratio >= 0.75f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                currencyFormat.format(totalSpent),
                style = MaterialTheme.typography.displayMedium
            )
            if (totalBudget != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "spent of ${currencyFormat.format(totalBudget)} budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { ratio.coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${(ratio * 100).toInt()}% used · $daysRemaining days remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    "$daysRemaining days remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
