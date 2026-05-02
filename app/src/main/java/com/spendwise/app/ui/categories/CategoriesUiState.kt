package com.spendwise.app.ui.categories

import com.spendwise.app.domain.model.Category

data class CategoriesUiState(
    val categories: List<CategoryWithStats> = emptyList(),
    val isLoading: Boolean = true,
    val editingCategory: Category? = null,
    val showEditSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletingCategory: Category? = null,
    val deleteExpenseCount: Int = 0
)

data class CategoryWithStats(
    val category: Category,
    val expenseCount: Int = 0,
    val totalSpent: Double = 0.0
)
