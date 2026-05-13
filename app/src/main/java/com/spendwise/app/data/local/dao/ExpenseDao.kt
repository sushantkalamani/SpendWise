package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for expense CRUD operations and queries.
 *
 * Provides reactive [Flow]-based reads for UI observation, suspend functions
 * for one-shot writes, and bulk operations for import/export workflows.
 */
@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    fun getTotalByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE upiRefId = :upiRef LIMIT 1")
    suspend fun getByUpiRef(upiRef: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE isRecurring = 1")
    suspend fun getRecurringExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getPaginated(limit: Int, offset: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getCountByCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getCountForDateRange(startDate: Long, endDate: Long): Int

    // --- New queries for v2 features ---

    /** Returns all expenses ordered by date descending. Used for full CSV export. */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    /**
     * Finds a potential duplicate during CSV import by matching on the four
     * key fields: timestamp, amount, description, and category.
     */
    @Query(
        "SELECT * FROM expenses WHERE date = :date AND amount = :amount " +
        "AND description = :description AND categoryId = :categoryId LIMIT 1"
    )
    suspend fun findDuplicate(
        date: Long,
        amount: Double,
        description: String,
        categoryId: Long?
    ): ExpenseEntity?

    /** Bulk insert for CSV import. Ignores rows that conflict on primary key. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(expenses: List<ExpenseEntity>): List<Long>

    /** Deletes every expense row. Used by "Clear All Data" in Settings. */
    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    /** Returns the total number of expenses. Used for empty-state checks. */
    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getTotalCount(): Int
}
