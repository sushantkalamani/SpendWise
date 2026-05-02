package com.spendwise.app.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.categories.components.CategoryEditSheet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(uiState.categories, key = { it.category.id }) { item ->
                val color = try { Color(android.graphics.Color.parseColor(item.category.colorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.primary }
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(item.category.name) },
                        supportingContent = { Text("${item.expenseCount} transactions · ${currencyFormat.format(item.totalSpent)}") },
                        leadingContent = {
                            Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(item.category.name.first().toString(), color = color, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.showEditSheet(item.category) }) {
                                Icon(Icons.Filled.Edit, "Edit")
                            }
                        }
                    )
                }
            }

            item {
                OutlinedButton(onClick = { viewModel.showAddSheet() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, "Add")
                    Spacer(Modifier.width(8.dp))
                    Text("Add Category")
                }
            }
        }
    }

    if (uiState.showEditSheet) {
        CategoryEditSheet(
            existingCategory = uiState.editingCategory,
            onSave = { name, icon, color -> viewModel.saveCategory(name, icon, color) },
            onDismiss = { viewModel.hideEditSheet() }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete ${uiState.deletingCategory?.name}?") },
            text = { Text(if (uiState.deleteExpenseCount > 0) "${uiState.deleteExpenseCount} expenses will lose their category." else "This category has no expenses.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteCategory() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissDeleteDialog() }) { Text("Cancel") } }
        )
    }
}
