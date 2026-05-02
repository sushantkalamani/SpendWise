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

class AnalyticsViewModel(
    private val monthPeriodUseCase: GetMonthPeriodUseCase,
    private val summaryUseCase: GetMonthlySummaryUseCase,
    private val breakdownUseCase: GetCategoryBreakdownUseCase,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

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
                _uiState.update { it.copy(dailyTotals = dailyTotals) }
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
}
