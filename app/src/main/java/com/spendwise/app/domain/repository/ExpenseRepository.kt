package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Domain-layer contract for expense persistence operations.
 *
 * Implementations must translate between domain [Expense] objects and the
 * underlying storage representation (Room entities, network DTOs, etc.).
 */
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

    // --- v2 additions ---

    /** Returns all expenses (no date filter). Used for full CSV export. */
    fun getAllExpenses(): Flow<List<Expense>>

    /** Checks whether an expense with matching key fields already exists (import dedup). */
    suspend fun findDuplicate(date: LocalDateTime, amount: Double, description: String, categoryId: Long?): Expense?

    /** Bulk-inserts expenses. Returns list of inserted row IDs (-1 for skipped duplicates). */
    suspend fun insertAll(expenses: List<Expense>): List<Long>

    /** Deletes every expense. Used by "Clear All Data". */
    suspend fun deleteAllExpenses()

    /** Returns total expense count. Used for empty-state checks. */
    suspend fun getTotalCount(): Int

    /** Renames (or merges) a tag across all expenses in a given category. */
    suspend fun renameTagForCategory(categoryId: Long, oldTag: String, newTag: String)
}
