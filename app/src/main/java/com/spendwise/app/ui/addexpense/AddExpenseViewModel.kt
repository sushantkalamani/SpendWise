package com.spendwise.app.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.*
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * ViewModel shared by the Add Expense sheet, Add Expense detail screen,
 * and Edit Expense screen.
 *
 * When [loadExpenseForEdit] is called, the form switches to edit mode:
 * - Fields are pre-populated from the existing expense
 * - [saveExpense] calls [ExpenseRepository.updateExpense] instead of insert
 *
 * [loadExpenseForDuplicate] pre-fills the form but clears the ID so a new
 * expense is created on save.
 */
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

    // ---- Field updaters ----

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

    // ---- Edit / Duplicate support ----

    /**
     * Loads an existing expense by [expenseId] and populates the form
     * in **edit mode**. The save button will call [updateExpense].
     */
    fun loadExpenseForEdit(expenseId: Long) {
        viewModelScope.launch {
            val expense = expenseRepository.getExpenseById(expenseId) ?: return@launch
            populateFromExpense(expense, isEdit = true)
        }
    }

    /**
     * Loads an existing expense by [expenseId] and populates the form
     * in **add mode** (ID is cleared). Useful for "Duplicate" flows
     * where the user wants a copy with modifications.
     */
    fun loadExpenseForDuplicate(expenseId: Long) {
        viewModelScope.launch {
            val expense = expenseRepository.getExpenseById(expenseId) ?: return@launch
            populateFromExpense(expense, isEdit = false)
        }
    }

    private fun populateFromExpense(expense: Expense, isEdit: Boolean) {
        val dateMillis = expense.date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        _uiState.update {
            it.copy(
                amount = String.format("%.0f", expense.amount),
                selectedCategory = expense.category,
                description = expense.description,
                selectedDate = dateMillis,
                paymentMethod = expense.paymentMethod,
                tags = expense.tags,
                isRecurring = expense.isRecurring,
                recurringInterval = expense.recurringInterval?.name,
                editingExpenseId = if (isEdit) expense.id else null,
                isEditMode = isEdit,
                amountError = false,
                categoryError = false,
                isSaving = false,
                saveSuccess = false
            )
        }
    }

    // ---- Save / Update ----

    /**
     * Validates and saves the expense.
     *
     * - In edit mode: calls [ExpenseRepository.updateExpense]
     * - In add mode: calls [ExpenseRepository.addExpense]
     */
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
                id = state.editingExpenseId ?: 0,
                amount = amount,
                category = state.selectedCategory,
                description = state.description,
                date = dateTime,
                paymentMethod = state.paymentMethod,
                tags = state.tags,
                isRecurring = state.isRecurring,
                recurringInterval = state.recurringInterval?.let {
                    try { RecurringInterval.valueOf(it) } catch (_: Exception) { null }
                },
                source = if (state.isEditMode) ExpenseSource.MANUAL else ExpenseSource.MANUAL
            )

            if (state.isEditMode && state.editingExpenseId != null) {
                expenseRepository.updateExpense(expense)
            } else {
                expenseRepository.addExpense(expense)
            }
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    /** Resets all form fields and exits edit mode. */
    fun resetState() {
        _uiState.value = AddExpenseUiState()
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }
}
