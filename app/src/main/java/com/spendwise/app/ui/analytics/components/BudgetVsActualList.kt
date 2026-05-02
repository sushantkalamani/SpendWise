package com.spendwise.app.ui.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetVsActualList(
    breakdown: List<CategorySpend>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    val categoriesWithBudget = breakdown.filter { it.budgetLimit != null && it.budgetLimit > 0 }

    if (categoriesWithBudget.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categoriesWithBudget.forEach { spend ->
            val ratio = spend.amount / spend.budgetLimit!!
            val progressColor = when {
                ratio >= 1.0 -> MaterialTheme.colorScheme.error
                ratio >= 0.75 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(spend.category.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    if (ratio >= 1.0) {
                        Icon(Icons.Filled.Warning, contentDescription = "Over budget", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        "${currencyFormat.format(spend.amount)} / ${currencyFormat.format(spend.budgetLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { ratio.toFloat().coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
