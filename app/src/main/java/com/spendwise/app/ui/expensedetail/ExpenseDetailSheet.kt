package com.spendwise.app.ui.expensedetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Expense
import java.text.NumberFormat
import java.util.Locale

/**
 * Modal bottom sheet displaying full details of a single [Expense].
 *
 * Provides three primary actions:
 * - **Edit** — opens the edit expense screen
 * - **Duplicate** — creates a copy with a new date
 * - **Delete** — shows a confirmation dialog before deleting
 *
 * @param expense The expense to display.
 * @param onDismiss Called when the sheet is dismissed.
 * @param onEdit Called with the expense ID when the user taps Edit.
 * @param onDuplicate Called with the expense when the user taps Duplicate.
 * @param onDelete Called with the expense when the user confirms deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailSheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit,
    onDuplicate: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val categoryColor = try {
        expense.category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
    } catch (_: Exception) {
        null
    } ?: MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Header: Amount + Category ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        currencyFormat.format(expense.amount),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = categoryColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    expense.category?.name?.first()?.toString() ?: "?",
                                    color = categoryColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Text(
                            expense.category?.name ?: "Uncategorized",
                            style = MaterialTheme.typography.titleSmall,
                            color = categoryColor
                        )
                    }
                }
            }

            HorizontalDivider()

            // ---- Detail rows ----
            DetailRow("Date", "${expense.date.dayOfMonth}/${expense.date.monthNumber}/${expense.date.year}")
            DetailRow("Time", String.format("%02d:%02d", expense.date.hour, expense.date.minute))
            DetailRow("Payment Method", expense.paymentMethod.name.replace("_", " "))

            if (expense.description.isNotBlank()) {
                DetailRow("Description", expense.description)
            }

            if (expense.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text("Tags", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        expense.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            DetailRow("Source", expense.source.name)

            if (expense.isRecurring) {
                DetailRow("Recurring", expense.recurringInterval?.name ?: "Yes")
            }

            if (!expense.upiRefId.isNullOrBlank()) {
                DetailRow("UPI Reference", expense.upiRefId!!)
            }

            if (!expense.merchantVpa.isNullOrBlank()) {
                DetailRow("Merchant VPA", expense.merchantVpa!!)
            }

            HorizontalDivider()

            // ---- Action buttons ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(expense.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = { onDuplicate(expense) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Duplicate")
                }
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }

    // ---- Delete confirmation dialog ----
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Expense?") },
            text = {
                Text("Delete ${currencyFormat.format(expense.amount)} spent on ${expense.category?.name ?: "Unknown"}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(expense)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/** A simple label–value row used within the expense detail sheet. */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
