package com.spendwise.app.domain.usecase

import com.spendwise.app.domain.model.Budget
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.MonthPeriod
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCategoryBreakdownUseCaseTest {

    private val expenseRepository: ExpenseRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()

    private val useCase = GetCategoryBreakdownUseCase(
        expenseRepository,
        categoryRepository,
        budgetRepository
    )

    @Test
    fun `invoke should group uncategorized expenses into synthetic category`() = runTest {
        // Arrange
        val startDate = LocalDate(2026, 5, 1)
        val endDate = LocalDate(2026, 5, 31)
        val period = MonthPeriod(startDate, endDate, "May 2026")

        val foodCategory = Category(id = 1, name = "Food", icon = "🍔", colorHex = "#FF5722")
        val categories = listOf(foodCategory)

        val expenses = listOf(
            Expense(
                id = 1,
                amount = 100.0,
                category = foodCategory,
                date = LocalDateTime(2026, 5, 15, 12, 0)
            ),
            Expense(
                id = 2,
                amount = 50.0,
                category = null, // Uncategorized
                date = LocalDateTime(2026, 5, 16, 12, 0)
            )
        )

        val budgets = emptyList<Budget>()

        every { expenseRepository.getExpensesByDateRange(startDate, endDate) } returns flowOf(expenses)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        every { budgetRepository.getAllBudgets() } returns flowOf(budgets)

        // Act
        val result = useCase.invoke(period).first()

        // Assert
        assertEquals(2, result.size)
        
        // Food CategorySpend
        val foodSpend = result.first { it.category.id == 1L }
        assertEquals(100.0, foodSpend.amount, 0.001)
        assertEquals(66.666, foodSpend.percentage, 0.1)

        // Uncategorized CategorySpend
        val uncategorizedSpend = result.first { it.category.id == -1L }
        assertEquals("Uncategorized", uncategorizedSpend.category.name)
        assertEquals("❓", uncategorizedSpend.category.icon)
        assertEquals("#9E9E9E", uncategorizedSpend.category.colorHex)
        assertEquals(50.0, uncategorizedSpend.amount, 0.001)
        assertEquals(33.333, uncategorizedSpend.percentage, 0.1)
    }

    @Test
    fun `invoke should return only categories with non-zero expenses`() = runTest {
        // Arrange
        val startDate = LocalDate(2026, 5, 1)
        val endDate = LocalDate(2026, 5, 31)
        val period = MonthPeriod(startDate, endDate, "May 2026")

        val foodCategory = Category(id = 1, name = "Food", icon = "🍔", colorHex = "#FF5722")
        val billsCategory = Category(id = 2, name = "Bills", icon = "💳", colorHex = "#3F51B5")
        val categories = listOf(foodCategory, billsCategory)

        val expenses = listOf(
            Expense(
                id = 1,
                amount = 100.0,
                category = foodCategory,
                date = LocalDateTime(2026, 5, 15, 12, 0)
            )
        )

        every { expenseRepository.getExpensesByDateRange(startDate, endDate) } returns flowOf(expenses)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        every { budgetRepository.getAllBudgets() } returns flowOf(emptyList())

        // Act
        val result = useCase.invoke(period).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals(1L, result[0].category.id)
        assertEquals(100.0, result[0].percentage, 0.001)
    }
}
