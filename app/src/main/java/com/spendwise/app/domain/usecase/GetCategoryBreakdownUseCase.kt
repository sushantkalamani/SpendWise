package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.Category
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

            val mappedCategoryIds = categories.map { it.id }.toSet()
            val uncategorizedTotal = expenses
                .filter { it.category == null || !mappedCategoryIds.contains(it.category.id) }
                .sumOf { it.amount }

            val categorySpends = categories.map { category ->
                val categoryTotal = expenses
                    .filter { it.category?.id == category.id }
                    .sumOf { it.amount }
                CategorySpend(
                    category = category,
                    amount = categoryTotal,
                    percentage = if (totalSpent > 0) (categoryTotal / totalSpent) * 100 else 0.0,
                    budgetLimit = budgetMap[category.id]?.monthlyLimit
                )
            }.filter { it.amount > 0 }.toMutableList()

            if (uncategorizedTotal > 0) {
                categorySpends.add(
                    CategorySpend(
                        category = Category(
                            id = -1,
                            name = "Uncategorized",
                            icon = "❓",
                            colorHex = "#9E9E9E",
                            sortOrder = Int.MAX_VALUE
                        ),
                        amount = uncategorizedTotal,
                        percentage = if (totalSpent > 0) (uncategorizedTotal / totalSpent) * 100 else 0.0,
                        budgetLimit = null
                    )
                )
            }

            categorySpends.sortedByDescending { it.amount }
        }
    }
}
