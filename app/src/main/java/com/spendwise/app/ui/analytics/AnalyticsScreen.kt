package com.spendwise.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.analytics.components.BudgetVsActualList
import com.spendwise.app.ui.analytics.components.CategoryDonutChart
import com.spendwise.app.ui.analytics.components.RenameTagDialog
import com.spendwise.app.ui.analytics.components.SpendingTrendChart
import com.spendwise.app.ui.components.MatteCard
import com.spendwise.app.ui.components.SectionHeader
import java.text.NumberFormat
import java.util.Locale

/**
 * Analytics screen with spending charts, category breakdown, and insights.
 *
 * When no expenses exist, shows a friendly empty state instead of blank charts.
 * The insights section displays top category, average daily spend, projected
 * month-end, and budget risk warnings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToHistory: (searchQuery: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    // Rename dialog state
    var renameDialogCategoryId by remember { mutableStateOf<Long?>(null) }
    var renameDialogTag by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    "Previous month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                uiState.currentPeriod?.label ?: "Loading...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    "Next month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!uiState.hasExpenses) {
            // ---- Empty state ----
            Box(
                Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Filled.PieChart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No analytics yet",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Charts and insights will appear here once you start tracking expenses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // ---- Insights card ----
            MatteCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Insights", icon = Icons.Filled.PieChart)

                    uiState.topSpendingCategory?.let { top ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Top Category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${top.category.name} (${top.percentage.toInt()}%)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Avg Daily Spend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currencyFormat.format(uiState.averageDailySpend), style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Projected Month-End", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currencyFormat.format(uiState.projectedMonthEnd), style = MaterialTheme.typography.bodyMedium)
                    }

                    // Budget risk warning
                    uiState.budgetRiskWarning?.let { warning ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Donut chart — clickable with tag breakdown
            MatteCard(modifier = Modifier.fillMaxWidth()) {
                CategoryDonutChart(
                    breakdown = uiState.categoryBreakdown,
                    totalAmount = uiState.summary?.totalSpent ?: 0.0,
                    tagBreakdowns = uiState.tagBreakdowns,
                    expandedCategoryId = uiState.expandedCategoryId,
                    onCategoryClick = viewModel::toggleExpandedCategory,
                    onTagSearch = { tag -> onNavigateToHistory(tag) },
                    onTagRename = { catId, oldTag ->
                        renameDialogCategoryId = catId
                        renameDialogTag = oldTag
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Spending trend
            MatteCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        text = "Spending Trend",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.weight(1f)
                        ) {
                            SegmentedButton(
                                selected = uiState.chartType == ChartType.BAR,
                                onClick = { if (uiState.chartType != ChartType.BAR) viewModel.toggleChartType() },
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BarChart,
                                    contentDescription = "Bar Chart",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            SegmentedButton(
                                selected = uiState.chartType == ChartType.LINE,
                                onClick = { if (uiState.chartType != ChartType.LINE) viewModel.toggleChartType() },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "Line Chart",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.weight(1f)
                        ) {
                            SegmentedButton(
                                selected = uiState.viewMode == ChartViewMode.DAILY,
                                onClick = { if (uiState.viewMode != ChartViewMode.DAILY) viewModel.toggleViewMode() },
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Daily", maxLines = 1)
                            }
                            SegmentedButton(
                                selected = uiState.viewMode == ChartViewMode.WEEKLY,
                                onClick = { if (uiState.viewMode != ChartViewMode.WEEKLY) viewModel.toggleViewMode() },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Weekly", maxLines = 1)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    SpendingTrendChart(
                        dailyTotals = uiState.dailyTotals,
                        viewMode = uiState.viewMode,
                        chartType = uiState.chartType
                    )
                }
            }

            // Budget vs Actual
            if (uiState.categoryBreakdown.any { it.budgetLimit != null }) {
                MatteCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Budget vs Actual", icon = Icons.Filled.Warning)
                        Spacer(Modifier.height(12.dp))
                        BudgetVsActualList(breakdown = uiState.categoryBreakdown)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // space for bottom nav
    }

    // ---- Rename Tag Dialog ----
    if (renameDialogCategoryId != null && renameDialogTag != null) {
        RenameTagDialog(
            currentTag = renameDialogTag!!,
            onDismiss = {
                renameDialogCategoryId = null
                renameDialogTag = null
            },
            onConfirm = { newTag ->
                viewModel.renameTag(renameDialogCategoryId!!, renameDialogTag!!, newTag)
                renameDialogCategoryId = null
                renameDialogTag = null
            }
        )
    }
}
