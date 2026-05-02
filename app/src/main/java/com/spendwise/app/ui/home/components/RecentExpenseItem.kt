package com.spendwise.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Expense
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RecentExpenseItem(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val categoryColor = try {
        expense.category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
    } catch (_: Exception) {
        null
    } ?: MaterialTheme.colorScheme.primary

    ListItem(
        headlineContent = {
            Text(
                expense.description.ifBlank { expense.category?.name ?: "Expense" },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            Text(
                "${expense.date.dayOfMonth}/${expense.date.monthNumber} · ${expense.paymentMethod.name}",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Surface(
                color = categoryColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        expense.category?.name?.first()?.toString() ?: "?",
                        color = categoryColor,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        },
        trailingContent = {
            Text(
                "-${currencyFormat.format(expense.amount)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        modifier = modifier
    )
}
