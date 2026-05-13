package com.spendwise.app.ui.analytics

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.ExpenseSummary
import com.spendwise.app.domain.model.MonthPeriod

/**
 * UI state for the Analytics screen.
 *
 * Extends the basic chart data with computed insights like top spending
 * category, average daily spend, projected month-end total, and budget
 * risk warnings.
 */
data class AnalyticsUiState(
    val currentPeriod: MonthPeriod? = null,
    val summary: ExpenseSummary? = null,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val viewMode: ChartViewMode = ChartViewMode.WEEKLY,
    val isLoading: Boolean = true,

    // Insights
    val topSpendingCategory: CategorySpend? = null,
    val averageDailySpend: Double = 0.0,
    val projectedMonthEnd: Double = 0.0,
    val budgetRiskWarning: String? = null,
    val hasExpenses: Boolean = false
)

/**
 * Represents a single bar in the spending trend chart.
 *
 * @property label X-axis label (e.g. "13" for day 13, "W2" for week 2).
 * @property amount Total spend for this period.
 */
data class DailyTotal(
    val label: String,
    val amount: Double
)

/** Toggle between daily and weekly chart grouping. */
enum class ChartViewMode { DAILY, WEEKLY }
