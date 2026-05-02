package com.spendwise.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { prefsRepository.isCalendarMode.collect { v -> _uiState.update { it.copy(isCalendarMode = v) } } }
        viewModelScope.launch { prefsRepository.salaryDay.collect { v -> _uiState.update { it.copy(salaryDay = v) } } }
        viewModelScope.launch { prefsRepository.themeMode.collect { v -> _uiState.update { it.copy(themeMode = v) } } }
        viewModelScope.launch { prefsRepository.isDynamicColor.collect { v -> _uiState.update { it.copy(isDynamicColor = v) } } }
        viewModelScope.launch { prefsRepository.reminderEnabled.collect { v -> _uiState.update { it.copy(reminderEnabled = v) } } }
        viewModelScope.launch { prefsRepository.reminderHour.collect { v -> _uiState.update { it.copy(reminderHour = v) } } }
        viewModelScope.launch { prefsRepository.reminderMinute.collect { v -> _uiState.update { it.copy(reminderMinute = v) } } }
        viewModelScope.launch { prefsRepository.monthlyIncome.collect { v -> _uiState.update { it.copy(monthlyIncome = v?.toLong()?.toString() ?: "") } } }
    }

    fun setCalendarMode(isCalendar: Boolean) { viewModelScope.launch { prefsRepository.setCalendarMode(isCalendar) } }
    fun setSalaryDay(day: Int) { viewModelScope.launch { prefsRepository.setSalaryDay(day) } }
    fun setThemeMode(mode: String) { viewModelScope.launch { prefsRepository.setThemeMode(mode) } }
    fun setDynamicColor(enabled: Boolean) { viewModelScope.launch { prefsRepository.setDynamicColor(enabled) } }
    fun setReminderEnabled(enabled: Boolean) { viewModelScope.launch { prefsRepository.setReminderEnabled(enabled) } }
    fun setReminderTime(hour: Int, minute: Int) { viewModelScope.launch { prefsRepository.setReminderTime(hour, minute) } }
    fun setMonthlyIncome(income: String) {
        viewModelScope.launch { prefsRepository.setMonthlyIncome(income.toDoubleOrNull()) }
    }
}
