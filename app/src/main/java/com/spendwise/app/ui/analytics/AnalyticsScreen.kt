package com.spendwise.app.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.analytics.components.BudgetVsActualList
import com.spendwise.app.ui.analytics.components.CategoryDonutChart
import com.spendwise.app.ui.analytics.components.SpendingTrendChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
            }
            Text(
                uiState.currentPeriod?.label ?: "Loading...",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Donut chart
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                CategoryDonutChart(
                    breakdown = uiState.categoryBreakdown,
                    totalAmount = uiState.summary?.totalSpent ?: 0.0,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Spending trend
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spending Trend", style = MaterialTheme.typography.titleMedium)
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = uiState.viewMode == ChartViewMode.DAILY,
                                onClick = { if (uiState.viewMode != ChartViewMode.DAILY) viewModel.toggleViewMode() },
                                shape = SegmentedButtonDefaults.itemShape(0, 2)
                            ) { Text("Daily") }
                            SegmentedButton(
                                selected = uiState.viewMode == ChartViewMode.WEEKLY,
                                onClick = { if (uiState.viewMode != ChartViewMode.WEEKLY) viewModel.toggleViewMode() },
                                shape = SegmentedButtonDefaults.itemShape(1, 2)
                            ) { Text("Weekly") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SpendingTrendChart(
                        dailyTotals = uiState.dailyTotals,
                        viewMode = uiState.viewMode
                    )
                }
            }

            // Budget vs Actual
            if (uiState.categoryBreakdown.any { it.budgetLimit != null }) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Budget vs Actual", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        BudgetVsActualList(breakdown = uiState.categoryBreakdown)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // space for bottom nav
    }
}
