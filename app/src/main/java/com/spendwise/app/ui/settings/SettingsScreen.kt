package com.spendwise.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Settings screen with user preferences and data management actions.
 *
 * The DATA section provides working Export, Import, Backup, Restore, and
 * Clear Data actions. Each operation shows loading state inline and
 * success/error feedback via a [Snackbar].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val versionName = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
        } catch (e: Exception) {
            "2.2.1"
        }
    }

    // Notification permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setReminderEnabled(isGranted)
    }

    // ---- SAF launchers ----

    // Export: create a new CSV file
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportToCsv(it) } }

    // Import: open an existing CSV file
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFromCsv(it) } }

    // Backup: create a new .db file
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.backupDatabase(it) } }

    // Restore: open an existing .db file
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.restoreDatabase(it) } }

    // ---- Snackbar feedback for all operations ----
    LaunchedEffect(uiState.exportStatus, uiState.importStatus, uiState.backupStatus, uiState.restoreStatus, uiState.clearDataStatus) {
        val status = listOf(
            uiState.exportStatus,
            uiState.importStatus,
            uiState.backupStatus,
            uiState.restoreStatus,
            uiState.clearDataStatus
        ).firstOrNull { it is OperationStatus.Success || it is OperationStatus.Error }

        when (status) {
            is OperationStatus.Success -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.dismissOperationStatus()
            }
            is OperationStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                viewModel.dismissOperationStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // MONTH CONFIGURATION
            Text("MONTH CONFIGURATION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Month mode", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = uiState.isCalendarMode, onClick = { viewModel.setCalendarMode(true) })
                        Text("Calendar (1st to 31st)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !uiState.isCalendarMode, onClick = { viewModel.setCalendarMode(false) })
                        Text("Salary-based")
                    }

                    AnimatedVisibility(visible = !uiState.isCalendarMode) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text("Salary credit day", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                FilledTonalButton(onClick = { viewModel.setSalaryDay((uiState.salaryDay - 1).coerceAtLeast(1)) }) { Text("\u2212") }
                                Text("${uiState.salaryDay}", style = MaterialTheme.typography.titleLarge)
                                FilledTonalButton(onClick = { viewModel.setSalaryDay((uiState.salaryDay + 1).coerceAtMost(28)) }) { Text("+") }
                            }
                            Text("Month runs ${uiState.salaryDay}th to ${uiState.salaryDay - 1}th", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // INCOME
            Text("INCOME", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.monthlyIncome,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*$"))) viewModel.setMonthlyIncome(it) },
                        label = { Text("Monthly Income") },
                        prefix = { Text("\u20B9") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // REMINDER
            Text("DAILY REMINDER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("EOD Reminder", style = MaterialTheme.typography.bodyMedium)
                            Text("Remind me to log expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        viewModel.setReminderEnabled(true)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.setReminderEnabled(enabled)
                                }
                            }
                        )
                    }
                    AnimatedVisibility(visible = uiState.reminderEnabled) {
                        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Time:", style = MaterialTheme.typography.bodyMedium)
                            Text(String.format("%02d:%02d", uiState.reminderHour, uiState.reminderMinute), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // APPEARANCE
            Text("APPEARANCE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEachIndexed { index, (value, label) ->
                            SegmentedButton(
                                selected = uiState.themeMode == value,
                                onClick = { viewModel.setThemeMode(value) },
                                shape = SegmentedButtonDefaults.itemShape(index, 3)
                            ) { Text(label) }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Dynamic colors", style = MaterialTheme.typography.bodyMedium)
                                Text("Colors from your wallpaper", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = uiState.isDynamicColor, onCheckedChange = viewModel::setDynamicColor)
                        }
                    }
                }
            }

            // DATA
            Text("DATA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // Export to CSV
                    ListItem(
                        headlineContent = { Text("Export to CSV") },
                        supportingContent = { Text("Save all expenses as CSV file") },
                        leadingContent = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                        trailingContent = {
                            if (uiState.exportStatus is OperationStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable {
                            exportLauncher.launch("spendwise_export.csv")
                        }
                    )
                    HorizontalDivider()

                    // Share Export
                    ListItem(
                        headlineContent = { Text("Share Export") },
                        supportingContent = { Text("Share CSV via apps") },
                        leadingContent = { Icon(Icons.Filled.Share, contentDescription = null) },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { viewModel.shareCsvExport() }
                    )
                    HorizontalDivider()

                    // Import from CSV
                    ListItem(
                        headlineContent = { Text("Import from CSV") },
                        supportingContent = { Text("Import expenses from a CSV file") },
                        leadingContent = { Icon(Icons.Filled.FileUpload, contentDescription = null) },
                        trailingContent = {
                            if (uiState.importStatus is OperationStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable {
                            importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                        }
                    )
                    HorizontalDivider()

                    // Backup
                    ListItem(
                        headlineContent = { Text("Backup") },
                        supportingContent = { Text("Save full database backup") },
                        leadingContent = { Icon(Icons.Filled.Backup, contentDescription = null) },
                        trailingContent = {
                            if (uiState.backupStatus is OperationStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable {
                            backupLauncher.launch("spendwise_backup.db")
                        }
                    )
                    HorizontalDivider()

                    // Restore from backup
                    ListItem(
                        headlineContent = { Text("Restore from backup") },
                        supportingContent = { Text("Replace data with a backup file") },
                        leadingContent = { Icon(Icons.Filled.RestorePage, contentDescription = null) },
                        trailingContent = {
                            if (uiState.restoreStatus is OperationStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable {
                            restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        }
                    )
                    HorizontalDivider()

                    // Clear all data
                    ListItem(
                        headlineContent = {
                            Text("Clear all data", color = MaterialTheme.colorScheme.error)
                        },
                        supportingContent = {
                            Text("Permanently delete all expenses and budgets")
                        },
                        leadingContent = {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        trailingContent = {
                            if (uiState.clearDataStatus is OperationStatus.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.clickable { viewModel.requestClearData() }
                    )
                }
            }

            // ABOUT
            Text("ABOUT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(headlineContent = { Text("SpendWise v$versionName") })
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ---- Import Preview Bottom Sheet ----
    if (uiState.showImportPreview && uiState.importPreviewResult != null) {
        val result = uiState.importPreviewResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportPreview() },
            title = { Text("Import Preview") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${result.validRows.size} valid rows ready to import", style = MaterialTheme.typography.bodyMedium)
                    if (result.invalidRows.isNotEmpty()) {
                        Text(
                            "${result.invalidRows.size} rows with errors (will be skipped)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        // Show first few errors
                        result.invalidRows.take(3).forEach { row ->
                            Text(
                                "Line ${row.lineNumber}: ${row.error}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (result.invalidRows.size > 3) {
                            Text(
                                "...and ${result.invalidRows.size - 3} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                    Text("Preview:", style = MaterialTheme.typography.labelMedium)
                    result.validRows.take(5).forEach { row ->
                        Text(
                            "₹${String.format("%.0f", row.amount)} · ${row.categoryName ?: "Unknown"} · ${row.date?.date}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (result.validRows.size > 5) {
                        Text("...and ${result.validRows.size - 5} more", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport() },
                    enabled = result.validRows.isNotEmpty()
                ) {
                    Text("Import ${result.validRows.size} rows")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissImportPreview() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---- Clear Data Confirmation Dialog ----
    if (uiState.showClearDataDialog) {
        var confirmText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDataDialog() },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All Data?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This will permanently delete all expenses and budgets. Categories will be preserved. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Type DELETE to confirm:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmText.isNotBlank() && confirmText != "DELETE"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmClearData() },
                    enabled = confirmText == "DELETE",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}


