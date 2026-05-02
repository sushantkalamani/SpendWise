package com.spendwise.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.repository.UserPreferencesRepository
import com.spendwise.app.domain.usecase.GetMonthPeriodUseCase
import com.spendwise.app.domain.usecase.GetMonthlySummaryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val monthPeriodUseCase: GetMonthPeriodUseCase,
    private val summaryUseCase: GetMonthlySummaryUseCase,
    private val expenseRepository: ExpenseRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val period = monthPeriodUseCase.getCurrentPeriod()
            _uiState.update { it.copy(currentPeriod = period) }
            loadPeriod(period)
        }
        viewModelScope.launch {
            prefsRepository.monthlyIncome.collect { income ->
                _uiState.update { it.copy(monthlyIncome = income) }
            }
        }
    }

    private fun loadPeriod(period: MonthPeriod) {
        _uiState.update { it.copy(currentPeriod = period, isLoading = true) }

        viewModelScope.launch {
            summaryUseCase.invoke(period).collect { summary ->
                _uiState.update {
                    it.copy(
                        totalSpent = summary.totalSpent,
                        totalBudget = summary.totalBudget,
                        daysRemaining = summary.daysRemaining,
                        topCategories = summary.categoryBreakdown.take(5),
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate)
                .collect { expenses ->
                    _uiState.update { it.copy(recentExpenses = expenses.take(10)) }
                }
        }
    }

    fun nextMonth() {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let { loadPeriod(monthPeriodUseCase.getNextPeriod(it)) }
        }
    }

    fun previousMonth() {
        viewModelScope.launch {
            _uiState.value.currentPeriod?.let { loadPeriod(monthPeriodUseCase.getPreviousPeriod(it)) }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseRepository.deleteExpense(expense) }
    }
}
