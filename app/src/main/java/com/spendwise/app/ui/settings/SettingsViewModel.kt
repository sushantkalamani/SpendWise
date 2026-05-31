package com.spendwise.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.app.data.backup.DatabaseBackupManager
import com.spendwise.app.data.export.CsvExporter
import com.spendwise.app.data.export.CsvImporter
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.ExpenseSource
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.domain.model.RecurringInterval
import com.spendwise.app.domain.repository.CategoryRepository
import com.spendwise.app.domain.repository.ExpenseRepository
import com.spendwise.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * ViewModel for the Settings screen.
 *
 * Handles user preference updates and data-management operations
 * (export, import, backup, restore, clear data). Each operation
 * updates [SettingsUiState] to drive loading indicators and
 * success/error snackbars.
 */
class SettingsViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val csvExporter: CsvExporter,
    private val csvImporter: CsvImporter,
    private val backupManager: DatabaseBackupManager
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

    // ---- Preference setters (unchanged) ----

    fun setCalendarMode(isCalendar: Boolean) { viewModelScope.launch { prefsRepository.setCalendarMode(isCalendar) } }
    fun setSalaryDay(day: Int) { viewModelScope.launch { prefsRepository.setSalaryDay(day) } }
    fun setThemeMode(mode: String) { viewModelScope.launch { prefsRepository.setThemeMode(mode) } }
    fun setDynamicColor(enabled: Boolean) { viewModelScope.launch { prefsRepository.setDynamicColor(enabled) } }
    fun setReminderEnabled(enabled: Boolean) { viewModelScope.launch { prefsRepository.setReminderEnabled(enabled) } }
    fun setReminderTime(hour: Int, minute: Int) { viewModelScope.launch { prefsRepository.setReminderTime(hour, minute) } }
    fun setMonthlyIncome(income: String) {
        viewModelScope.launch {
            val incomeVal = income.toDoubleOrNull()?.coerceIn(0.0, 1_000_000_000.0)
            prefsRepository.setMonthlyIncome(incomeVal)
        }
    }

    // ---- Export CSV ----

    /**
     * Exports all expenses to the user-chosen SAF [uri] as CSV.
     * Updates [SettingsUiState.exportStatus] throughout the operation.
     */
    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportStatus = OperationStatus.Loading) }
            try {
                val expenses = expenseRepository.getAllExpenses().first()
                if (expenses.isEmpty()) {
                    _uiState.update { it.copy(exportStatus = OperationStatus.Error("No expenses to export")) }
                    return@launch
                }
                val success = withContext(Dispatchers.IO) {
                    csvExporter.exportToUri(expenses, uri)
                }
                _uiState.update {
                    it.copy(
                        exportStatus = if (success) OperationStatus.Success("Exported ${expenses.size} expenses")
                        else OperationStatus.Error("Failed to write CSV file")
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportStatus = OperationStatus.Error("Export failed: ${e.message}")) }
            }
        }
    }

    /**
     * Exports to cache and opens the system share sheet.
     */
    fun shareCsvExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(exportStatus = OperationStatus.Loading) }
            try {
                val expenses = expenseRepository.getAllExpenses().first()
                if (expenses.isEmpty()) {
                    _uiState.update { it.copy(exportStatus = OperationStatus.Error("No expenses to export")) }
                    return@launch
                }
                val file = withContext(Dispatchers.IO) {
                    csvExporter.exportToCache(expenses)
                }
                csvExporter.shareFile(file)
                _uiState.update { it.copy(exportStatus = OperationStatus.Success("Shared ${expenses.size} expenses")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportStatus = OperationStatus.Error("Share failed: ${e.message}")) }
            }
        }
    }

    // ---- Import CSV ----

    /**
     * Parses a CSV from the given [uri] and populates the import preview.
     * Does NOT save data yet — the user must confirm via [confirmImport].
     */
    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(importStatus = OperationStatus.Loading) }
            try {
                val result = csvImporter.parseFromUri(uri)
                if (result.validRows.isEmpty() && result.invalidRows.isEmpty()) {
                    _uiState.update { it.copy(importStatus = OperationStatus.Error("CSV file is empty or unreadable")) }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        importStatus = OperationStatus.Idle,
                        showImportPreview = true,
                        importPreviewResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(importStatus = OperationStatus.Error("Import parse failed: ${e.message}")) }
            }
        }
    }

    /**
     * Saves the valid rows from the import preview, skipping duplicates.
     * Called after the user reviews the preview and taps "Import".
     */
    fun confirmImport() {
        val result = _uiState.value.importPreviewResult ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(importStatus = OperationStatus.Loading, showImportPreview = false) }
            try {
                val categories = categoryRepository.getAllCategories().first()
                val categoryMap = categories.associateBy { it.name.normalizedCategoryKey() }.toMutableMap()

                var importedCount = 0
                var createdCategoryCount = 0
                var skippedDuplicates = 0

                for (row in result.validRows) {
                    val date = row.date ?: continue
                    val amount = row.amount ?: continue
                    val category = row.categoryName
                        ?.takeUnless { it.equals("Uncategorized", ignoreCase = true) }
                        ?.let { rawName ->
                            val categoryName = rawName.trim()
                            val key = categoryName.normalizedCategoryKey()
                            categoryMap[key] ?: run {
                                val sortOrder = categoryMap.values.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
                                val newCategory = Category(
                                    name = categoryName,
                                    icon = inferCategoryIcon(categoryName),
                                    colorHex = colorForImportedCategory(categoryName),
                                    sortOrder = sortOrder
                                )
                                val id = categoryRepository.addCategory(newCategory)
                                newCategory.copy(id = id).also {
                                    categoryMap[key] = it
                                    createdCategoryCount++
                                }
                            }
                    }

                    // Duplicate check
                    val existing = expenseRepository.findDuplicate(
                        date = date,
                        amount = amount,
                        description = row.description ?: "",
                        categoryId = category?.id
                    )
                    if (existing != null) {
                        skippedDuplicates++
                        continue
                    }

                    val expense = Expense(
                        amount = amount,
                        category = category,
                        description = row.description ?: "",
                        date = date,
                        paymentMethod = row.paymentMethod?.let {
                            try { PaymentMethod.valueOf(it) } catch (_: Exception) { PaymentMethod.OTHER }
                        } ?: PaymentMethod.OTHER,
                        tags = row.tags,
                        source = ExpenseSource.IMPORT,
                        isRecurring = row.isRecurring,
                        recurringInterval = row.recurringInterval?.let {
                            try { RecurringInterval.valueOf(it) } catch (_: Exception) { null }
                        },
                        upiRefId = row.upiRefId,
                        merchantVpa = row.merchantVpa
                    )
                    expenseRepository.addExpense(expense)
                    importedCount++
                }

                val msg = buildString {
                    append("Imported $importedCount expenses")
                    if (createdCategoryCount > 0) append(", created $createdCategoryCount categories")
                    if (skippedDuplicates > 0) append(", skipped $skippedDuplicates duplicates")
                    if (result.invalidRows.isNotEmpty()) append(", ${result.invalidRows.size} rows had errors")
                }
                _uiState.update { it.copy(importStatus = OperationStatus.Success(msg), importPreviewResult = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(importStatus = OperationStatus.Error("Import failed: ${e.message}")) }
            }
        }
    }

    /** Dismisses the import preview without saving. */
    fun dismissImportPreview() {
        _uiState.update { it.copy(showImportPreview = false, importPreviewResult = null) }
    }

    private fun String.normalizedCategoryKey(): String = trim().lowercase()

    private fun inferCategoryIcon(name: String): String {
        val normalized = name.lowercase()
        return when {
            listOf("food", "dining", "restaurant", "coffee", "cafe").any { it in normalized } -> "Restaurant"
            listOf("transport", "cab", "taxi", "fuel", "car", "metro").any { it in normalized } -> "DirectionsCar"
            listOf("bill", "utility", "rent", "subscription").any { it in normalized } -> "Receipt"
            listOf("shop", "clothes", "fashion").any { it in normalized } -> "ShoppingBag"
            listOf("health", "doctor", "medical", "pharmacy").any { it in normalized } -> "LocalHospital"
            listOf("movie", "entertainment", "game").any { it in normalized } -> "Movie"
            listOf("grocery", "groceries", "market").any { it in normalized } -> "ShoppingCart"
            listOf("travel", "flight", "trip").any { it in normalized } -> "Flight"
            listOf("school", "education", "course").any { it in normalized } -> "School"
            listOf("pet", "pets").any { it in normalized } -> "Pets"
            else -> "MoreHoriz"
        }
    }

    private fun colorForImportedCategory(name: String): String {
        val palette = listOf(
            "#4CAF50", "#2196F3", "#FF9800", "#E91E63",
            "#F44336", "#9C27B0", "#8BC34A", "#607D8B",
            "#00BCD4", "#FFEB3B", "#795548", "#3F51B5"
        )
        val index = Math.floorMod(name.normalizedCategoryKey().hashCode(), palette.size)
        return palette[index]
    }

    // ---- Backup / Restore ----

    /**
     * Backs up the database to the user-chosen SAF [uri].
     */
    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(backupStatus = OperationStatus.Loading) }
            val success = backupManager.backupTo(uri)
            _uiState.update {
                it.copy(
                    backupStatus = if (success) OperationStatus.Success("Backup saved successfully")
                    else OperationStatus.Error("Backup failed")
                )
            }
        }
    }

    /** Shows the restore confirmation dialog. */
    fun requestRestore() {
        _uiState.update { it.copy(showRestoreConfirmDialog = true) }
    }

    /**
     * Restores the database from the chosen SAF [uri].
     * **Warning**: Replaces all existing data.
     */
    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(restoreStatus = OperationStatus.Loading, showRestoreConfirmDialog = false) }
            val success = backupManager.restoreFrom(uri)
            if (success) {
                _uiState.update { it.copy(restoreStatus = OperationStatus.Success("Restore complete — restarting...")) }
                backupManager.restartApp()
            } else {
                _uiState.update { it.copy(restoreStatus = OperationStatus.Error("Restore failed")) }
            }
        }
    }

    // ---- Clear Data ----

    /** Shows the typed-confirmation dialog for clearing all data. */
    fun requestClearData() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    /** Dismisses the clear-data dialog without clearing. */
    fun dismissClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    /**
     * Actually clears all expenses and budgets after the user has typed
     * the confirmation word. Categories are preserved.
     */
    fun confirmClearData() {
        viewModelScope.launch {
            _uiState.update { it.copy(clearDataStatus = OperationStatus.Loading, showClearDataDialog = false) }
            val success = backupManager.clearAllData()
            _uiState.update {
                it.copy(
                    clearDataStatus = if (success) OperationStatus.Success("All data cleared")
                    else OperationStatus.Error("Clear data failed")
                )
            }
        }
    }

    /** Resets any operation status back to Idle (called after snackbar is dismissed). */
    fun dismissOperationStatus() {
        _uiState.update {
            it.copy(
                exportStatus = OperationStatus.Idle,
                importStatus = OperationStatus.Idle,
                backupStatus = OperationStatus.Idle,
                restoreStatus = OperationStatus.Idle,
                clearDataStatus = OperationStatus.Idle
            )
        }
    }
}
