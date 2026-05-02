package com.spendwise.app.domain.repository

import com.spendwise.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetForCategory(categoryId: Long): Flow<Budget?>
    fun getOverallBudget(): Flow<Budget?>
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun setBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)
}
