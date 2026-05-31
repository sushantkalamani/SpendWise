package com.spendwise.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.components.CategoryIconBadge
import com.spendwise.app.ui.components.MatteCard
import com.spendwise.app.ui.components.SectionHeader
import com.spendwise.app.ui.components.rememberCategoryColor
import com.spendwise.app.ui.categories.components.CategoryEditSheet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            item {
                SectionHeader("Categories", icon = Icons.Filled.Category)
            }
            items(uiState.categories, key = { it.category.id }) { item ->
                val color = rememberCategoryColor(item.category.colorHex)
                MatteCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(item.category.name) },
                        supportingContent = {
                            Text(
                                "${item.expenseCount} transactions · ${currencyFormat.format(item.totalSpent)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            CategoryIconBadge(
                                iconName = item.category.icon,
                                contentDescription = item.category.name,
                                color = color,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.showEditSheet(item.category) }) {
                                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.showAddSheet() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
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
