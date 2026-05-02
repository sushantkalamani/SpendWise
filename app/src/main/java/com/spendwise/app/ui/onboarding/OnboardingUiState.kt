package com.spendwise.app.ui.onboarding

import com.spendwise.app.domain.model.Category

data class OnboardingUiState(
    val currentPage: Int = 0,
    val userName: String = "",
    val monthlyIncome: String = "",
    val salaryDay: Int = 1,
    val isCalendarMode: Boolean = true,
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val overallBudget: String = "",
    val isComplete: Boolean = false,
    val nameError: Boolean = false
)
