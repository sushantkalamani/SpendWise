package com.spendwise.app.ui.home

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.MonthPeriod

data class HomeUiState(
    val currentPeriod: MonthPeriod? = null,
    val totalSpent: Double = 0.0,
    val totalBudget: Double? = null,
    val daysRemaining: Int = 0,
    val monthlyIncome: Double? = null,
    val topCategories: List<CategorySpend> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true
)
