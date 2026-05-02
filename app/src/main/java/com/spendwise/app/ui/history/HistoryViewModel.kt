package com.spendwise.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.usecase.GetMonthPeriodUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val monthPeriodUseCase: GetMonthPeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }

        viewModelScope.launch {
            val period = monthPeriodUseCase.getCurrentPeriod()
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate).collect { expenses ->
                _uiState.update { it.copy(expenses = expenses, isLoading = false) }
            }
        }

        viewModelScope.launch {
            _searchQuery.debounce(300).collectLatest { query ->
                if (query.isBlank()) {
                    val period = monthPeriodUseCase.getCurrentPeriod()
                    expenseRepository.getExpensesByDateRange(period.startDate, period.endDate).collect { expenses ->
                        _uiState.update { it.copy(expenses = expenses) }
                    }
                } else {
                    expenseRepository.searchExpenses(query).collect { expenses ->
                        _uiState.update { it.copy(expenses = expenses) }
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    fun toggleSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
    }

    fun toggleCategoryFilter(categoryId: Long) {
        _uiState.update { state ->
            val newSet = if (categoryId in state.selectedCategoryIds)
                state.selectedCategoryIds - categoryId
            else
                state.selectedCategoryIds + categoryId
            state.copy(selectedCategoryIds = newSet)
        }
    }

    fun setPaymentMethodFilter(method: PaymentMethod?) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseRepository.deleteExpense(expense) }
    }

    fun getFilteredExpenses(): List<Expense> {
        val state = _uiState.value
        var filtered = state.expenses
        if (state.selectedCategoryIds.isNotEmpty()) {
            filtered = filtered.filter { it.category?.id in state.selectedCategoryIds }
        }
        if (state.selectedPaymentMethod != null) {
            filtered = filtered.filter { it.paymentMethod == state.selectedPaymentMethod }
        }
        return filtered
    }
}
