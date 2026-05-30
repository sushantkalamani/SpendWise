package com.spendwise.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.usecase.GetMonthPeriodUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * ViewModel for the History screen.
 *
 * Provides search, category/payment-method filtering, date-range filtering,
 * sorting, and delete-with-undo for the expense history list.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val monthPeriodUseCase: GetMonthPeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _customDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)

    /** Holds the most recently deleted expense for undo support. */
    private val _lastDeletedExpense = MutableStateFlow<Expense?>(null)
    val lastDeletedExpense: StateFlow<Expense?> = _lastDeletedExpense.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }

        // Single reactive pipeline to fetch expenses based on query and date range
        val expensesFlow = combine(
            _searchQuery.debounce { if (it.isEmpty()) 0L else 300L },
            _customDateRange
        ) { query, range ->
            Pair(query, range)
        }.flatMapLatest { (query, range) ->
            _uiState.update { it.copy(isLoading = true) }
            if (query.isNotBlank()) {
                if (range != null) {
                    expenseRepository.searchExpenses(query).map { list ->
                        list.filter { it.date.date in range.first..range.second }
                    }
                } else {
                    expenseRepository.searchExpenses(query)
                }
            } else if (range != null) {
                expenseRepository.getExpensesByDateRange(range.first, range.second)
            } else {
                val period = monthPeriodUseCase.getCurrentPeriod()
                expenseRepository.getExpensesByDateRange(period.startDate, period.endDate)
            }
        }

        viewModelScope.launch {
            expensesFlow.collect { expenses ->
                _uiState.update { it.copy(expenses = expenses, isLoading = false) }
            }
        }

        // Check if user has any expenses at all (for empty-state differentiation)
        viewModelScope.launch {
            val count = expenseRepository.getTotalCount()
            _uiState.update { it.copy(hasNoExpensesAtAll = count == 0) }
        }
    }

    // ---- Search ----

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    fun toggleSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
    }

    // ---- Category / Payment Filters ----

    fun toggleCategoryFilter(categoryId: Long) {
        _uiState.update { state ->
            val newSet = if (categoryId in state.selectedCategoryIds)
                state.selectedCategoryIds - categoryId
            else
                state.selectedCategoryIds + categoryId
            state.copy(selectedCategoryIds = newSet).recalcFilterCount()
        }
    }

    fun setPaymentMethodFilter(method: PaymentMethod?) {
        _uiState.update { it.copy(selectedPaymentMethod = method).recalcFilterCount() }
    }

    // ---- Date Range Filter ----

    /**
     * Sets a custom date range filter.
     *
     * When set, only expenses within [start]–[end] are shown.
     * Also reloads expenses from the repository for that range.
     */
    fun setDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(dateRangeStart = start, dateRangeEnd = end).recalcFilterCount() }
        _customDateRange.value = Pair(start, end)
    }

    /** Clears the date range filter, reverting to the current month. */
    fun clearDateRange() {
        _uiState.update { it.copy(dateRangeStart = null, dateRangeEnd = null).recalcFilterCount() }
        _customDateRange.value = null
    }

    // ---- Sort ----

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option).recalcFilterCount() }
    }

    // ---- Clear All Filters ----

    /** Resets all filters and sort to defaults. */
    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedCategoryIds = emptySet(),
                selectedPaymentMethod = null,
                dateRangeStart = null,
                dateRangeEnd = null,
                sortOption = SortOption.NEWEST,
                searchQuery = "",
                activeFilterCount = 0
            )
        }
        _searchQuery.value = ""
        _customDateRange.value = null
    }

    // ---- Delete with undo ----

    fun deleteExpenseWithUndo(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            _lastDeletedExpense.value = expense
        }
    }

    fun undoDelete() {
        val expense = _lastDeletedExpense.value ?: return
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
            _lastDeletedExpense.value = null
        }
    }

    fun clearDeletedExpense() {
        _lastDeletedExpense.value = null
    }

    @Deprecated("Use deleteExpenseWithUndo instead", ReplaceWith("deleteExpenseWithUndo(expense)"))
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseRepository.deleteExpense(expense) }
    }

    // ---- Filtered & sorted output ----

    /**
     * Returns the expense list after applying all active filters and sort.
     *
     * Filtering order: category → payment method → sort.
     * Date range filtering is handled at the repository level via [setDateRange].
     */
    fun getFilteredExpenses(): List<Expense> {
        val state = _uiState.value
        var filtered = state.expenses

        if (state.selectedCategoryIds.isNotEmpty()) {
            filtered = filtered.filter { it.category?.id in state.selectedCategoryIds }
        }
        if (state.selectedPaymentMethod != null) {
            filtered = filtered.filter { it.paymentMethod == state.selectedPaymentMethod }
        }

        return when (state.sortOption) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.date }
            SortOption.OLDEST -> filtered.sortedBy { it.date }
            SortOption.AMOUNT_HIGH -> filtered.sortedByDescending { it.amount }
            SortOption.AMOUNT_LOW -> filtered.sortedBy { it.amount }
        }
    }

    /** Recalculates the number of active (non-default) filters. */
    private fun HistoryUiState.recalcFilterCount(): HistoryUiState {
        var count = 0
        if (selectedCategoryIds.isNotEmpty()) count++
        if (selectedPaymentMethod != null) count++
        if (dateRangeStart != null) count++
        if (sortOption != SortOption.NEWEST) count++
        return copy(activeFilterCount = count)
    }
}
