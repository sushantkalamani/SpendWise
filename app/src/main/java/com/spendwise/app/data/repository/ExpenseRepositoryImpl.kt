package com.spendwise.app.data.repository

import com.spendwise.app.data.local.dao.CategoryDao
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.data.local.entity.ExpenseEntity
import com.spendwise.app.domain.model.*
import com.spendwise.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*

/**
 * Room-backed implementation of [ExpenseRepository].
 *
 * Handles mapping between domain [Expense] objects and [ExpenseEntity] rows,
 * including category look-ups for the domain model's embedded [Category].
 */
class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insert(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getById(id)?.toDomain()
    }

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getByDateRange(startDate.toEpochMillis(), endDate.toEndOfDayMillis())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesByCategoryAndDateRange(
        categoryId: Long, startDate: LocalDate, endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseDao.getByCategoryAndDateRange(categoryId, startDate.toEpochMillis(), endDate.toEndOfDayMillis())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun searchExpenses(query: String): Flow<List<Expense>> {
        return expenseDao.search(query).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTotalByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double?> {
        return expenseDao.getTotalByDateRange(startDate.toEpochMillis(), endDate.toEndOfDayMillis())
    }

    override fun getTotalByCategoryAndDateRange(
        categoryId: Long, startDate: LocalDate, endDate: LocalDate
    ): Flow<Double?> {
        return expenseDao.getTotalByCategoryAndDateRange(categoryId, startDate.toEpochMillis(), endDate.toEndOfDayMillis())
    }

    override suspend fun getByUpiRef(upiRef: String): Expense? {
        return expenseDao.getByUpiRef(upiRef)?.toDomain()
    }

    override suspend fun getRecurringExpenses(): List<Expense> {
        return expenseDao.getRecurringExpenses().map { it.toDomain() }
    }

    override fun getPaginated(limit: Int, offset: Int): Flow<List<Expense>> {
        return expenseDao.getPaginated(limit, offset).map { entities -> entities.map { it.toDomain() } }
    }

    // --- v2 additions ---

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun findDuplicate(
        date: LocalDateTime,
        amount: Double,
        description: String,
        categoryId: Long?
    ): Expense? {
        val dateMillis = date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        return expenseDao.findDuplicate(dateMillis, amount, description, categoryId)?.toDomain()
    }

    override suspend fun insertAll(expenses: List<Expense>): List<Long> {
        return expenseDao.insertAll(expenses.map { it.toEntity() })
    }

    override suspend fun deleteAllExpenses() {
        expenseDao.deleteAll()
    }

    override suspend fun getTotalCount(): Int {
        return expenseDao.getTotalCount()
    }

    // ---- Entity ↔ Domain mappers ----

    private suspend fun ExpenseEntity.toDomain(): Expense {
        val category = categoryId?.let { categoryDao.getById(it) }
        return Expense(
            id = id,
            amount = amount,
            category = category?.let { Category(it.id, it.name, it.icon, it.colorHex, it.sortOrder) },
            description = description,
            date = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()),
            paymentMethod = PaymentMethod.valueOf(paymentMethod),
            tags = if (tags.isBlank()) emptyList() else tags.split(","),
            upiRefId = upiRefId,
            merchantVpa = merchantVpa,
            source = ExpenseSource.valueOf(source),
            isRecurring = isRecurring,
            recurringInterval = recurringInterval?.let { RecurringInterval.valueOf(it) }
        )
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            amount = amount,
            categoryId = category?.id,
            description = description,
            date = date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            paymentMethod = paymentMethod.name,
            tags = tags.joinToString(","),
            upiRefId = upiRefId,
            merchantVpa = merchantVpa,
            source = source.name,
            isRecurring = isRecurring,
            recurringInterval = recurringInterval?.name
        )
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    private fun LocalDate.toEndOfDayMillis(): Long {
        return this.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() - 1
    }
}
