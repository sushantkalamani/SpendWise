package com.spendwise.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.history.components.DateGroupHeader
import com.spendwise.app.ui.history.components.FilterChipsRow
import com.spendwise.app.ui.home.components.RecentExpenseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredExpenses = viewModel.getFilteredExpenses()

    // Group by date
    val grouped = filteredExpenses.groupBy {
        "${it.date.dayOfMonth}/${it.date.monthNumber}/${it.date.year}"
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = { viewModel.toggleSearchActive(false) },
                    expanded = uiState.isSearchActive,
                    onExpandedChange = viewModel::toggleSearchActive,
                    placeholder = { Text("Search expenses...") },
                    leadingIcon = { Icon(Icons.Filled.Search, "Search") }
                )
            },
            expanded = uiState.isSearchActive,
            onExpandedChange = viewModel::toggleSearchActive,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {}

        // Filter chips
        FilterChipsRow(
            categories = uiState.categories,
            selectedCategoryIds = uiState.selectedCategoryIds,
            selectedPaymentMethod = uiState.selectedPaymentMethod,
            onCategoryToggle = viewModel::toggleCategoryFilter,
            onPaymentMethodSelect = viewModel::setPaymentMethodFilter,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredExpenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No expenses found", style = MaterialTheme.typography.titleMedium)
                    Text("Try adjusting your filters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                grouped.forEach { (dateLabel, expenses) ->
                    item(key = "header_$dateLabel") {
                        DateGroupHeader(dateLabel = dateLabel, totalAmount = expenses.sumOf { it.amount })
                    }
                    items(expenses, key = { it.id }) { expense ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteExpense(expense)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxSize()) {
                                    Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                        Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            RecentExpenseItem(expense = expense)
                        }
                    }
                }
            }
        }
    }
}
