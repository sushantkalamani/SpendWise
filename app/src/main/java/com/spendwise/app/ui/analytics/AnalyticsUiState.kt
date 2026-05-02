package com.spendwise.app.ui.analytics

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.ExpenseSummary
import com.spendwise.app.domain.model.MonthPeriod

data class AnalyticsUiState(
    val currentPeriod: MonthPeriod? = null,
    val summary: ExpenseSummary? = null,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val viewMode: ChartViewMode = ChartViewMode.WEEKLY,
    val isLoading: Boolean = true
)

data class DailyTotal(
    val label: String,
    val amount: Double
)

enum class ChartViewMode { DAILY, WEEKLY }
