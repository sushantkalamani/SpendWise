package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

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
}
