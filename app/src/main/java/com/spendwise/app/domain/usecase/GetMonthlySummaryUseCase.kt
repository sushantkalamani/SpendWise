package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.ExpenseSummary
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*

class GetMonthlySummaryUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    fun invoke(period: MonthPeriod): Flow<ExpenseSummary> {
        return combine(
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate),
            categoryRepository.getAllCategories(),
            budgetRepository.getAllBudgets(),
            budgetRepository.getOverallBudget()
        ) { expenses, categories, budgets, overallBudget ->
            val totalSpent = expenses.sumOf { it.amount }
            val budgetMap = budgets.associateBy { it.categoryId }

            val categoryBreakdown = categories.map { category ->
                val categoryTotal = expenses
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
                val percentage = if (totalSpent > 0) (categoryTotal / totalSpent) * 100 else 0.0
                CategorySpend(
                    category = category,
                    amount = categoryTotal,
                    percentage = percentage,
                    budgetLimit = budgetMap[category.id]?.monthlyLimit
                )
            }.filter { it.amount > 0 }
                .sortedByDescending { it.amount }

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val daysRemaining = if (today <= period.endDate) {
                period.endDate.toEpochDays() - today.toEpochDays()
            } else 0

            ExpenseSummary(
                totalSpent = totalSpent,
                totalBudget = overallBudget?.monthlyLimit,
                categoryBreakdown = categoryBreakdown,
                daysRemaining = daysRemaining.toInt()
            )
        }
    }
}
