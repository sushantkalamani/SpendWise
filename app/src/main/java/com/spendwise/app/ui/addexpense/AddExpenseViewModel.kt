package com.spendwise.app.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.*
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, amountError = false) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, categoryError = false) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateDate(dateMillis: Long) {
        _uiState.update { it.copy(selectedDate = dateMillis) }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun addTag(tag: String) {
        if (tag.isBlank()) return
        _uiState.update { it.copy(tags = it.tags + tag.trim(), newTagText = "") }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun updateNewTagText(text: String) {
        _uiState.update { it.copy(newTagText = text) }
    }

    fun toggleDetailedMode() {
        _uiState.update { it.copy(isDetailedMode = !it.isDetailedMode) }
    }

    fun toggleRecurring() {
        _uiState.update { it.copy(isRecurring = !it.isRecurring) }
    }

    fun updateRecurringInterval(interval: String?) {
        _uiState.update { it.copy(recurringInterval = interval) }
    }

    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = true) }
            return
        }
        if (state.selectedCategory == null) {
            _uiState.update { it.copy(categoryError = true) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val dateTime = Instant.fromEpochMilliseconds(state.selectedDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())

            val expense = Expense(
                amount = amount,
                category = state.selectedCategory,
                description = state.description,
                date = dateTime,
                paymentMethod = state.paymentMethod,
                tags = state.tags,
                isRecurring = state.isRecurring,
                recurringInterval = state.recurringInterval?.let {
                    try { RecurringInterval.valueOf(it) } catch (_: Exception) { null }
                }
            )
            expenseRepository.addExpense(expense)
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun resetState() {
        _uiState.value = AddExpenseUiState()
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }
}
