package com.spendwise.app.ui.history

import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.PaymentMethod

data class HistoryUiState(
    val searchQuery: String = "",
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true
)
