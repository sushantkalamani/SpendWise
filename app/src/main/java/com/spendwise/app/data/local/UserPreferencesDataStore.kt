package com.spendwise.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spendwise_prefs")

class UserPreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val SALARY_DAY = intPreferencesKey("salary_day")
        val IS_CALENDAR_MODE = booleanPreferencesKey("is_calendar_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_DYNAMIC_COLOR = booleanPreferencesKey("is_dynamic_color")
        val IS_UPI_SYNC_ENABLED = booleanPreferencesKey("is_upi_sync_enabled")
        val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val userName: Flow<String> = dataStore.data.map { it[USER_NAME] ?: "" }
    val salaryDay: Flow<Int> = dataStore.data.map { it[SALARY_DAY] ?: 1 }
    val isCalendarMode: Flow<Boolean> = dataStore.data.map { it[IS_CALENDAR_MODE] ?: true }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "system" }
    val isDynamicColor: Flow<Boolean> = dataStore.data.map { it[IS_DYNAMIC_COLOR] ?: true }
    val isUpiSyncEnabled: Flow<Boolean> = dataStore.data.map { it[IS_UPI_SYNC_ENABLED] ?: false }
    val monthlyIncome: Flow<Double?> = dataStore.data.map { it[MONTHLY_INCOME] }
    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { it[IS_ONBOARDING_COMPLETE] ?: false }
    val reminderEnabled: Flow<Boolean> = dataStore.data.map { it[REMINDER_ENABLED] ?: true }
    val reminderHour: Flow<Int> = dataStore.data.map { it[REMINDER_HOUR] ?: 21 }
    val reminderMinute: Flow<Int> = dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }

    suspend fun setUserName(name: String) { dataStore.edit { it[USER_NAME] = name } }
    suspend fun setSalaryDay(day: Int) { dataStore.edit { it[SALARY_DAY] = day } }
    suspend fun setCalendarMode(isCalendar: Boolean) { dataStore.edit { it[IS_CALENDAR_MODE] = isCalendar } }
    suspend fun setThemeMode(mode: String) { dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setDynamicColor(enabled: Boolean) { dataStore.edit { it[IS_DYNAMIC_COLOR] = enabled } }
    suspend fun setUpiSyncEnabled(enabled: Boolean) { dataStore.edit { it[IS_UPI_SYNC_ENABLED] = enabled } }
    suspend fun setMonthlyIncome(income: Double?) {
        dataStore.edit {
            if (income != null) it[MONTHLY_INCOME] = income
            else it.remove(MONTHLY_INCOME)
        }
    }
    suspend fun setOnboardingComplete(complete: Boolean) { dataStore.edit { it[IS_ONBOARDING_COMPLETE] = complete } }
    suspend fun setReminderEnabled(enabled: Boolean) { dataStore.edit { it[REMINDER_ENABLED] = enabled } }
    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[REMINDER_HOUR] = hour
            it[REMINDER_MINUTE] = minute
        }
    }
}
