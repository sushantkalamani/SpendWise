package com.spendwise.app.data.repository

import com.spendwise.app.data.local.UserPreferencesDataStore
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {
    override val userName: Flow<String> = dataStore.userName
    override val salaryDay: Flow<Int> = dataStore.salaryDay
    override val isCalendarMode: Flow<Boolean> = dataStore.isCalendarMode
    override val themeMode: Flow<String> = dataStore.themeMode
    override val isDynamicColor: Flow<Boolean> = dataStore.isDynamicColor
    override val isUpiSyncEnabled: Flow<Boolean> = dataStore.isUpiSyncEnabled
    override val monthlyIncome: Flow<Double?> = dataStore.monthlyIncome
    override val isOnboardingComplete: Flow<Boolean> = dataStore.isOnboardingComplete
    override val reminderEnabled: Flow<Boolean> = dataStore.reminderEnabled
    override val reminderHour: Flow<Int> = dataStore.reminderHour
    override val reminderMinute: Flow<Int> = dataStore.reminderMinute

    override suspend fun setUserName(name: String) = dataStore.setUserName(name)
    override suspend fun setSalaryDay(day: Int) = dataStore.setSalaryDay(day)
    override suspend fun setCalendarMode(isCalendar: Boolean) = dataStore.setCalendarMode(isCalendar)
    override suspend fun setThemeMode(mode: String) = dataStore.setThemeMode(mode)
    override suspend fun setDynamicColor(enabled: Boolean) = dataStore.setDynamicColor(enabled)
    override suspend fun setUpiSyncEnabled(enabled: Boolean) = dataStore.setUpiSyncEnabled(enabled)
    override suspend fun setMonthlyIncome(income: Double?) = dataStore.setMonthlyIncome(income)
    override suspend fun setOnboardingComplete(complete: Boolean) = dataStore.setOnboardingComplete(complete)
    override suspend fun setReminderEnabled(enabled: Boolean) = dataStore.setReminderEnabled(enabled)
    override suspend fun setReminderTime(hour: Int, minute: Int) = dataStore.setReminderTime(hour, minute)
}
