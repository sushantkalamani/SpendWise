package com.spendwise.app.ui.settings

import com.spendwise.app.data.export.CsvImporter

/**
 * UI state for the Settings screen.
 *
 * Tracks user preferences, data-operation progress, and dialog visibility.
 */
data class SettingsUiState(
    // Month configuration
    val isCalendarMode: Boolean = true,
    val salaryDay: Int = 1,

    // Appearance
    val themeMode: String = "system",
    val isDynamicColor: Boolean = true,

    // Reminder
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,

    // Income
    val monthlyIncome: String = "",

    // Data operation feedback
    val exportStatus: OperationStatus = OperationStatus.Idle,
    val importStatus: OperationStatus = OperationStatus.Idle,
    val backupStatus: OperationStatus = OperationStatus.Idle,
    val restoreStatus: OperationStatus = OperationStatus.Idle,
    val clearDataStatus: OperationStatus = OperationStatus.Idle,

    // Dialogs & previews
    val showClearDataDialog: Boolean = false,
    val showImportPreview: Boolean = false,
    val importPreviewResult: CsvImporter.ImportResult? = null,
    val showRestoreConfirmDialog: Boolean = false
)

/**
 * Represents the lifecycle of an async data operation (export, import, etc.).
 */
sealed class OperationStatus {
    /** No operation in progress and no result to show. */
    data object Idle : OperationStatus()

    /** Operation is running — show a progress indicator. */
    data object Loading : OperationStatus()

    /** Operation completed successfully. [message] is shown in a snackbar. */
    data class Success(val message: String) : OperationStatus()

    /** Operation failed. [message] describes the error for the user. */
    data class Error(val message: String) : OperationStatus()
}
