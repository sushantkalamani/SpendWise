package com.spendwise.app.ui.settings

data class SettingsUiState(
    val isCalendarMode: Boolean = true,
    val salaryDay: Int = 1,
    val themeMode: String = "system",
    val isDynamicColor: Boolean = true,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val monthlyIncome: String = ""
)
