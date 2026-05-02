package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun getExpenseById(id: Long): Expense?
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getExpensesByCategoryAndDateRange(categoryId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun searchExpenses(query: String): Flow<List<Expense>>
    fun getTotalByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double?>
    fun getTotalByCategoryAndDateRange(categoryId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double?>
    suspend fun getByUpiRef(upiRef: String): Expense?
    suspend fun getRecurringExpenses(): List<Expense>
    fun getPaginated(limit: Int, offset: Int): Flow<List<Expense>>
}
