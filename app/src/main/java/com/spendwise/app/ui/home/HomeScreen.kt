package com.spendwise.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.home.components.MonthSummaryCard
import com.spendwise.app.ui.home.components.RecentExpenseItem
import com.spendwise.app.ui.home.components.TopCategoryChips
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Month selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous")
                    }
                    Text(
                        uiState.currentPeriod?.label ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, "Settings")
                }
            }
        }

        // Summary card
        item {
            MonthSummaryCard(
                totalSpent = uiState.totalSpent,
                totalBudget = uiState.totalBudget,
                daysRemaining = uiState.daysRemaining
            )
        }

        // Income / Savings row
        if (uiState.monthlyIncome != null) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Income",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                currencyFormat.format(uiState.monthlyIncome),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    OutlinedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Savings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val savings = (uiState.monthlyIncome ?: 0.0) - uiState.totalSpent
                            Text(
                                currencyFormat.format(savings),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (savings >= 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }
        }

        // Top categories
        if (uiState.topCategories.isNotEmpty()) {
            item {
                TopCategoryChips(categories = uiState.topCategories)
            }
        }

        // Recent expenses header
        if (uiState.recentExpenses.isNotEmpty()) {
            item {
                Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Recent expenses list with swipe-to-delete
        items(uiState.recentExpenses, key = { it.id }) { expense ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        viewModel.deleteExpense(expense)
                        true
                    } else {
                        false
                    }
                }
            )
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterEnd,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
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
