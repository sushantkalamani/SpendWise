package com.spendwise.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.usecase.GetMonthPeriodUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val monthPeriodUseCase: GetMonthPeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            val period = monthPeriodUseCase.getCurrentPeriod()
            categoryRepository.getAllCategories().combine(
                expenseRepository.getExpensesByDateRange(period.startDate, period.endDate)
            ) { categories, expenses ->
                categories.map { cat ->
                    val catExpenses = expenses.filter { it.category?.id == cat.id }
                    CategoryWithStats(cat, catExpenses.size, catExpenses.sumOf { it.amount })
                }
            }.collect { stats ->
                _uiState.update { it.copy(categories = stats, isLoading = false) }
            }
        }
    }

    fun showAddSheet() { _uiState.update { it.copy(editingCategory = null, showEditSheet = true) } }
    fun showEditSheet(category: Category) { _uiState.update { it.copy(editingCategory = category, showEditSheet = true) } }
    fun hideEditSheet() { _uiState.update { it.copy(showEditSheet = false, editingCategory = null) } }

    fun saveCategory(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            val existing = _uiState.value.editingCategory
            if (existing != null) {
                categoryRepository.updateCategory(existing.copy(name = name, icon = icon, colorHex = colorHex))
            } else {
                val sortOrder = _uiState.value.categories.size
                categoryRepository.addCategory(Category(name = name, icon = icon, colorHex = colorHex, sortOrder = sortOrder))
            }
            hideEditSheet()
        }
    }

    fun confirmDelete(category: Category) {
        viewModelScope.launch {
            val count = categoryRepository.getExpenseCountForCategory(category.id)
            _uiState.update { it.copy(showDeleteDialog = true, deletingCategory = category, deleteExpenseCount = count) }
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            _uiState.value.deletingCategory?.let { categoryRepository.deleteCategory(it) }
            _uiState.update { it.copy(showDeleteDialog = false, deletingCategory = null) }
        }
    }

    fun dismissDeleteDialog() { _uiState.update { it.copy(showDeleteDialog = false, deletingCategory = null) } }
}
