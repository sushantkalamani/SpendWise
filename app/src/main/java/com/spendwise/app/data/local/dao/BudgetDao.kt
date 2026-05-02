package com.spendwise.app.data.local.dao

import androidx.room.*
import com.spendwise.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    fun getByCategoryId(categoryId: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE isOverallBudget = 1 LIMIT 1")
    fun getOverallBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets")
    fun getAll(): Flow<List<BudgetEntity>>
}
