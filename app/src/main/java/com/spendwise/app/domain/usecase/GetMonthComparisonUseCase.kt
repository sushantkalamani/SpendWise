package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first

data class MonthComparison(
    val periodA: MonthPeriod,
    val periodB: MonthPeriod,
    val totalA: Double,
    val totalB: Double,
    val deltaPercentage: Double,
    val categoryComparisons: List<CategoryComparison>
)

data class CategoryComparison(
    val categoryName: String,
    val colorHex: String,
    val amountA: Double,
    val amountB: Double,
    val deltaPercentage: Double
)

class GetMonthComparisonUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend fun compare(periodA: MonthPeriod, periodB: MonthPeriod): MonthComparison {
        val expensesA = expenseRepository.getExpensesByDateRange(periodA.startDate, periodA.endDate).first()
        val expensesB = expenseRepository.getExpensesByDateRange(periodB.startDate, periodB.endDate).first()
        val categories = categoryRepository.getAllCategories().first()

        val totalA = expensesA.sumOf { it.amount }
        val totalB = expensesB.sumOf { it.amount }
        val totalDelta = if (totalA > 0) ((totalB - totalA) / totalA) * 100 else 0.0

        val categoryComparisons = categories.map { cat ->
            val amtA = expensesA.filter { it.category?.id == cat.id }.sumOf { it.amount }
            val amtB = expensesB.filter { it.category?.id == cat.id }.sumOf { it.amount }
            val delta = if (amtA > 0) ((amtB - amtA) / amtA) * 100 else if (amtB > 0) 100.0 else 0.0
            CategoryComparison(cat.name, cat.colorHex, amtA, amtB, delta)
        }.filter { it.amountA > 0 || it.amountB > 0 }
            .sortedByDescending { maxOf(it.amountA, it.amountB) }

        return MonthComparison(periodA, periodB, totalA, totalB, totalDelta, categoryComparisons)
    }
}
