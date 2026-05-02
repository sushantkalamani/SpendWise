package com.spendwise.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.model.Budget
import com.spendwise.app.domain.repository.BudgetRepository
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats, selectedCategoryIds = cats.map { c -> c.id }.toSet()) }
            }
        }
    }

    fun updateName(name: String) { _uiState.update { it.copy(userName = name, nameError = false) } }
    fun updateIncome(income: String) { _uiState.update { it.copy(monthlyIncome = income) } }
    fun updateSalaryDay(day: Int) { _uiState.update { it.copy(salaryDay = day) } }
    fun toggleCalendarMode(isCalendar: Boolean) { _uiState.update { it.copy(isCalendarMode = isCalendar) } }
    fun toggleCategory(categoryId: Long) {
        _uiState.update { state ->
            val newSet = if (categoryId in state.selectedCategoryIds)
                state.selectedCategoryIds - categoryId
            else
                state.selectedCategoryIds + categoryId
            state.copy(selectedCategoryIds = newSet)
        }
    }
    fun updateBudget(budget: String) { _uiState.update { it.copy(overallBudget = budget) } }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage == 1 && state.userName.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
    }

    fun previousPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            prefsRepository.setUserName(state.userName)
            state.monthlyIncome.toDoubleOrNull()?.let { prefsRepository.setMonthlyIncome(it) }
            prefsRepository.setSalaryDay(state.salaryDay)
            prefsRepository.setCalendarMode(state.isCalendarMode)
            state.overallBudget.toDoubleOrNull()?.let { budget ->
                budgetRepository.setBudget(Budget(categoryId = 0, monthlyLimit = budget, isOverallBudget = true))
            }
            prefsRepository.setOnboardingComplete(true)
            _uiState.update { it.copy(isComplete = true) }
        }
    }
}
