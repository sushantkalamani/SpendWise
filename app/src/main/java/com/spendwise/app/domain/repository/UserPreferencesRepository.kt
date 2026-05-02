package com.spendwise.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userName: Flow<String>
    val salaryDay: Flow<Int>
    val isCalendarMode: Flow<Boolean>
    val themeMode: Flow<String>
    val isDynamicColor: Flow<Boolean>
    val isUpiSyncEnabled: Flow<Boolean>
    val monthlyIncome: Flow<Double?>
    val isOnboardingComplete: Flow<Boolean>
    val reminderEnabled: Flow<Boolean>
    val reminderHour: Flow<Int>
    val reminderMinute: Flow<Int>

    suspend fun setUserName(name: String)
    suspend fun setSalaryDay(day: Int)
    suspend fun setCalendarMode(isCalendar: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setUpiSyncEnabled(enabled: Boolean)
    suspend fun setMonthlyIncome(income: Double?)
    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
}
