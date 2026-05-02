package com.spendwise.app.data.repository

import com.spendwise.app.data.local.dao.BudgetDao
import com.spendwise.app.data.local.entity.BudgetEntity
import com.spendwise.app.domain.model.Budget
import com.spendwise.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgetForCategory(categoryId: Long): Flow<Budget?> {
        return budgetDao.getByCategoryId(categoryId).map { it?.toDomain() }
    }

    override fun getOverallBudget(): Flow<Budget?> {
        return budgetDao.getOverallBudget().map { it?.toDomain() }
    }

    override fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun setBudget(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget.toEntity())
    }

    private fun BudgetEntity.toDomain(): Budget {
        return Budget(id = id, categoryId = categoryId, monthlyLimit = monthlyLimit, isOverallBudget = isOverallBudget)
    }

    private fun Budget.toEntity(): BudgetEntity {
        return BudgetEntity(id = id, categoryId = categoryId, monthlyLimit = monthlyLimit, isOverallBudget = isOverallBudget)
    }
}
