package com.spendwise.app.ui.addexpense

import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.PaymentMethod

/**
 * UI state for both Add and Edit expense flows.
 *
 * When [isEditMode] is true, the form is pre-populated from an existing
 * expense and the save button calls update instead of insert.
 */
data class AddExpenseUiState(
    val amount: String = "",
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val description: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val tags: List<String> = emptyList(),
    val newTagText: String = "",
    val isDetailedMode: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // Edit mode fields
    val editingExpenseId: Long? = null,
    val isEditMode: Boolean = false
)
