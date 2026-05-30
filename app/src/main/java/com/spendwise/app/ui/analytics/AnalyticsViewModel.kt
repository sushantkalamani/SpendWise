package com.spendwise.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.usecase.GetCategoryBreakdownUseCase
import com.spendwise.app.domain.usecase.GetMonthPeriodUseCase
import com.spendwise.app.domain.usecase.GetMonthlySummaryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.text.NumberFormat
import java.util.Locale

/**
 * ViewModel for the Analytics screen.
 *
 * Loads chart data (donut, trend) and computes spending insights:
 * - Top spending category
 * - Average daily spend (based on elapsed days, not total period)
 * - Projected month-end spend (linear extrapolation)
 * - Budget risk warning when projected > budget
 *
 * Weekly grouping uses ISO week numbers rather than naive 7-day chunking.
 */
class AnalyticsViewModel(
    private val monthPeriodUseCase: GetMonthPeriodUseCase,
    private val summaryUseCase: GetMonthlySummaryUseCase,
    private val breakdownUseCase: GetCategoryBreakdownUseCase,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    init {
        viewModelScope.launch {
            val period = monthPeriodUseCase.getCurrentPeriod()
            loadPeriod(period)
        }
    }

    private fun loadPeriod(period: MonthPeriod) {
        _uiState.update { it.copy(currentPeriod = period, isLoading = true) }

        viewModelScope.launch {
            summaryUseCase.invoke(period).collect { summary ->
                _uiState.update { it.copy(summary = summary) }
            }
        }

        viewModelScope.launch {
            breakdownUseCase.invoke(period).collect { breakdown ->
                _uiState.update { it.copy(categoryBreakdown = breakdown, isLoading = false) }
            }
        }

        viewModelScope.launch {
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate).collect { expenses ->
                val hasExpenses = expenses.isNotEmpty()

                // ---- Daily totals (fills zero-spend days) ----
                val grouped = expenses.groupBy { it.date.date }
                val dailyTotals = mutableListOf<DailyTotal>()
                var current = period.startDate
                while (current <= period.endDate) {
                    val dayExpenses = grouped[current] ?: emptyList()
                    dailyTotals.add(DailyTotal(
                        label = "${current.dayOfMonth}",
                        amount = dayExpenses.sumOf { it.amount }
                    ))
                    current = current.plus(1, DateTimeUnit.DAY)
                }

                // ---- Compute insights ----
                val totalSpent = expenses.sumOf { it.amount }
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val elapsedDays = if (today >= period.startDate) {
                    (minOf(today, period.endDate).toEpochDays() - period.startDate.toEpochDays() + 1).coerceAtLeast(1)
                } else 1
                val totalDays = (period.endDate.toEpochDays() - period.startDate.toEpochDays() + 1).coerceAtLeast(1)

                val avgDaily = if (elapsedDays > 0) totalSpent / elapsedDays else 0.0
                val projected = avgDaily * totalDays

                // Top category
                val categoryBreakdown = _uiState.value.categoryBreakdown
                val topCategory = categoryBreakdown.maxByOrNull { it.amount }

                // Budget risk warning
                val budgetLimit = _uiState.value.summary?.totalBudget
                val budgetWarning = if (budgetLimit != null && budgetLimit > 0 && projected > budgetLimit) {
                    val excess = projected - budgetLimit
                    "At current pace, you may exceed budget by ${currencyFormat.format(excess)}"
                } else null

                // ---- Tag-wise breakdown per category ----
                val tagBreakdowns = mutableMapOf<Long, List<TagSpend>>()
                val byCat = expenses.groupBy { it.category?.id }
                byCat.forEach { (catId, catExpenses) ->
                    if (catId == null) return@forEach
                    val catTotal = catExpenses.sumOf { it.amount }
                    if (catTotal <= 0) return@forEach

                    // Accumulate amounts per normalized tag
                    val tagAmounts = mutableMapOf<String, Double>()
                    catExpenses.forEach { exp ->
                        if (exp.tags.isEmpty()) {
                            tagAmounts["Untagged"] = (tagAmounts["Untagged"] ?: 0.0) + exp.amount
                        } else {
                            exp.tags.forEach { rawTag ->
                                val normalized = rawTag.trim().uppercase()
                                tagAmounts[normalized] = (tagAmounts[normalized] ?: 0.0) + exp.amount
                            }
                        }
                    }

                    tagBreakdowns[catId] = tagAmounts.entries
                        .map { (tag, amount) ->
                            TagSpend(
                                tag = tag,
                                amount = amount,
                                percentage = (amount / catTotal) * 100
                            )
                        }
                        .sortedByDescending { it.amount }
                }

                _uiState.update {
                    it.copy(
                        dailyTotals = dailyTotals,
                        hasExpenses = hasExpenses,
                        topSpendingCategory = topCategory,
                        averageDailySpend = avgDaily,
                        projectedMonthEnd = projected,
                        budgetRiskWarning = budgetWarning,
                        tagBreakdowns = tagBreakdowns
                    )
                }
            }
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let { current ->
                loadPeriod(monthPeriodUseCase.getNextPeriod(current))
            }
        }
    }

    fun previousMonth() {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let { current ->
                loadPeriod(monthPeriodUseCase.getPreviousPeriod(current))
            }
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(viewMode = if (it.viewMode == ChartViewMode.DAILY) ChartViewMode.WEEKLY else ChartViewMode.DAILY)
        }
    }

    fun toggleChartType() {
        _uiState.update {
            it.copy(chartType = if (it.chartType == ChartType.BAR) ChartType.LINE else ChartType.BAR)
        }
    }

    /** Toggles the expanded/collapsed state of a category in the breakdown list. */
    fun toggleExpandedCategory(categoryId: Long) {
        _uiState.update {
            it.copy(
                expandedCategoryId = if (it.expandedCategoryId == categoryId) null else categoryId
            )
        }
    }

    /** Renames a tag across all expenses in a category, then reloads the current period. */
    fun renameTag(categoryId: Long, oldTag: String, newTag: String) {
        viewModelScope.launch {
            expenseRepository.renameTagForCategory(categoryId, oldTag, newTag)
            // Reload to reflect changes
            _uiState.value.currentPeriod?.let { loadPeriod(it) }
        }
    }
}
