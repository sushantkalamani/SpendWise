package com.spendwise.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.ui.expensedetail.ExpenseDetailSheet
import com.spendwise.app.ui.home.components.MonthSummaryCard
import com.spendwise.app.ui.home.components.RecentExpenseItem
import com.spendwise.app.ui.home.components.TopCategoryChips
import com.spendwise.app.ui.components.MetricTile
import com.spendwise.app.ui.components.SectionHeader
import java.text.NumberFormat
import java.util.Locale

/**
 * Home screen showing the monthly summary, income/savings, top categories,
 * and recent expenses.
 *
 * Tapping an expense opens the [ExpenseDetailSheet] with Edit/Duplicate/Delete
 * actions. Delete shows a confirmation dialog and an undo snackbar.
 *
 * When no expenses exist, a friendly empty state prompts the user to add
 * their first expense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit = {},
    onEditExpense: (Long) -> Unit = {},
    onDuplicateExpense: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    // Show undo snackbar when an expense is deleted
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month Selector & Profile Top Bar (Fixed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        "Previous",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    uiState.currentPeriod?.label ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        "Next",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    Icons.Filled.AccountCircle,
                    "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recentExpenses.isEmpty() && uiState.totalSpent == 0.0) {
                // ---- Empty state ----
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Track your first expense!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap the + button to add an expense and start tracking your spending.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // ---- Main content ----
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {

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
                            MetricTile(
                                label = "Income",
                                value = currencyFormat.format(uiState.monthlyIncome),
                                icon = Icons.Filled.AccountBalanceWallet,
                                modifier = Modifier.weight(1f),
                                accent = MaterialTheme.colorScheme.secondary
                            )
                            val savings = (uiState.monthlyIncome ?: 0.0) - uiState.totalSpent
                            MetricTile(
                                label = "Savings",
                                value = currencyFormat.format(savings),
                                icon = Icons.Filled.Savings,
                                modifier = Modifier.weight(1f),
                                accent = if (savings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
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
                        SectionHeader("Recent Expenses", icon = Icons.AutoMirrored.Filled.ReceiptLong)
                    }
                }

                // Recent expenses list — tap to open detail (NO swipe-to-delete)
                items(uiState.recentExpenses, key = { it.id }) { expense ->
                    RecentExpenseItem(
                        expense = expense,
                        onClick = { selectedExpense = expense }
                    )
                }
            }
        }

        // Snackbar host pinned to bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
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
}
