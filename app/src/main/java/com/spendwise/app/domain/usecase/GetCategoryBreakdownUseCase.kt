package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*

class GetCategoryBreakdownUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    fun invoke(period: MonthPeriod): Flow<List<CategorySpend>> {
        return combine(
            expenseRepository.getExpensesByDateRange(period.startDate, period.endDate),
            categoryRepository.getAllCategories(),
            budgetRepository.getAllBudgets()
        ) { expenses, categories, budgets ->
            val totalSpent = expenses.sumOf { it.amount }
            val budgetMap = budgets.associateBy { it.categoryId }

            categories.map { category ->
                val categoryTotal = expenses
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
                CategorySpend(
                    category = category,
                    amount = categoryTotal,
                    percentage = if (totalSpent > 0) (categoryTotal / totalSpent) * 100 else 0.0,
                    budgetLimit = budgetMap[category.id]?.monthlyLimit
                )
            }.filter { it.amount > 0 }
                .sortedByDescending { it.amount }
        }
    }
}
