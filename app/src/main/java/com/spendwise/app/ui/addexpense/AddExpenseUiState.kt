package com.spendwise.app.ui.addexpense

import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.PaymentMethod

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
    val saveSuccess: Boolean = false
)
