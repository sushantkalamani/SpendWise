package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.Budget
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first

data class BudgetAlert(
    val categoryName: String,
    val spent: Double,
    val limit: Double,
    val percentage: Int,
    val isOverBudget: Boolean
)

class CheckBudgetAlertUseCase(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend fun check(period: MonthPeriod): List<BudgetAlert> {
        val budgets = budgetRepository.getAllBudgets().first()
        val alerts = mutableListOf<BudgetAlert>()

        for (budget in budgets) {
            if (budget.isOverallBudget) {
                val totalSpent = expenseRepository.getTotalByDateRange(period.startDate, period.endDate).first() ?: 0.0
                val pct = ((totalSpent / budget.monthlyLimit) * 100).toInt()
                if (pct >= 80) {
                    alerts.add(BudgetAlert("Overall", totalSpent, budget.monthlyLimit, pct, pct >= 100))
                }
            } else {
                val spent = expenseRepository.getTotalByCategoryAndDateRange(budget.categoryId, period.startDate, period.endDate).first() ?: 0.0
                val pct = ((spent / budget.monthlyLimit) * 100).toInt()
                if (pct >= 80) {
                    alerts.add(BudgetAlert("Category", spent, budget.monthlyLimit, pct, pct >= 100))
                }
            }
        }
        return alerts
    }
}
