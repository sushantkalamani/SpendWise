package com.spendwise.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.ui.expensedetail.ExpenseDetailSheet
import com.spendwise.app.ui.history.components.DateGroupHeader
import com.spendwise.app.ui.history.components.FilterChipsRow
import com.spendwise.app.ui.home.components.RecentExpenseItem
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

/**
 * History screen showing all expenses with search, filters, sort, and
 * the expense detail bottom sheet.
 *
 * Distinguishes between two empty states:
 * - No expenses at all → prompts user to add first expense
 * - No results for current filters → shows "no results" with a clear-filters button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEditExpense: (Long) -> Unit = {},
    onDuplicateExpense: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredExpenses = viewModel.getFilteredExpenses()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    // Group by date
    val grouped = filteredExpenses.groupBy {
        "${it.date.dayOfMonth}/${it.date.monthNumber}/${it.date.year}"
    }

    // Undo snackbar
    val deletedExpense by viewModel.lastDeletedExpense.collectAsState()
    LaunchedEffect(deletedExpense) {
        deletedExpense?.let { expense ->
            val result = snackbarHostState.showSnackbar(
                message = "Deleted ${currencyFormat.format(expense.amount)}",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeletedExpense()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            // Filter chips with new sort/date-range support
            Box {
                FilterChipsRow(
                    categories = uiState.categories,
                    selectedCategoryIds = uiState.selectedCategoryIds,
                    selectedPaymentMethod = uiState.selectedPaymentMethod,
                    sortOption = uiState.sortOption,
                    activeFilterCount = uiState.activeFilterCount,
                    hasDateRange = uiState.dateRangeStart != null,
                    onCategoryToggle = viewModel::toggleCategoryFilter,
                    onPaymentMethodSelect = viewModel::setPaymentMethodFilter,
                    onDateRangeClick = { showDateRangePicker = true },
                    onSortClick = { showSortMenu = true },
                    onClearFilters = viewModel::clearAllFilters,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Sort dropdown menu
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                viewModel.setSortOption(option)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (uiState.sortOption == option) {
                                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredExpenses.isEmpty()) {
                // ---- Empty states ----
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.hasNoExpensesAtAll) {
                        // No expenses at all
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("No expenses yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Start tracking by tapping the + button",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // No results for current filters
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(
                                Icons.Filled.FilterListOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("No expenses match your filters", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(onClick = viewModel::clearAllFilters) {
                                Text("Clear filters")
                            }
                        }
                    }
                }
            } else {
                // ---- Expense list ----
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    grouped.forEach { (dateLabel, expenses) ->
                        item(key = "header_$dateLabel") {
                            DateGroupHeader(dateLabel = dateLabel, totalAmount = expenses.sumOf { it.amount })
                        }
                        items(expenses, key = { it.id }) { expense ->
                            RecentExpenseItem(
                                expense = expense,
                                onClick = { selectedExpense = expense }
                            )
                        }
                    }
                }
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }

    // ---- Expense detail bottom sheet ----
    selectedExpense?.let { expense ->
        ExpenseDetailSheet(
            expense = expense,
            onDismiss = { selectedExpense = null },
            onEdit = { id ->
                selectedExpense = null
                onEditExpense(id)
            },
            onDuplicate = { exp ->
                selectedExpense = null
                onDuplicateExpense(exp.id)
            },
            onDelete = { exp ->
                selectedExpense = null
                viewModel.deleteExpenseWithUndo(exp)
            }
        )
    }

    // ---- Date Range Picker dialog ----
    if (showDateRangePicker) {
        val dateRangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = dateRangeState.selectedStartDateMillis
                    val endMillis = dateRangeState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val tz = TimeZone.currentSystemDefault()
                        val start = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(tz).date
                        val end = Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(tz).date
                        viewModel.setDateRange(start, end)
                    }
                    showDateRangePicker = false
                }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearDateRange()
                    showDateRangePicker = false
                }) { Text("Clear") }
            }
        ) {
            DateRangePicker(state = dateRangeState, modifier = Modifier.height(500.dp))
        }
    }
}
