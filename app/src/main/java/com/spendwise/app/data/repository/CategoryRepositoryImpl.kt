package com.spendwise.app.data.repository

import com.spendwise.app.data.local.dao.CategoryDao
import com.spendwise.app.data.local.dao.ExpenseDao
import com.spendwise.app.data.local.entity.CategoryEntity
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getByName(name)?.toDomain()
    }

    override suspend fun addCategory(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category.toEntity())
    }

    override suspend fun getExpenseCountForCategory(categoryId: Long): Int {
        return expenseDao.getCountByCategory(categoryId)
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(id = id, name = name, icon = icon, colorHex = colorHex, sortOrder = sortOrder)
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(id = id, name = name, icon = icon, colorHex = colorHex, sortOrder = sortOrder)
    }
}
