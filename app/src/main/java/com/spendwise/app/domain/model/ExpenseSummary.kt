package com.spendwise.app.domain.model

data class ExpenseSummary(
    val totalSpent: Double,
    val totalBudget: Double?,
    val categoryBreakdown: List<CategorySpend>,
    val daysRemaining: Int
)

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Double,
    val budgetLimit: Double?
)
