package com.spendwise.app.ui.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
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
                        Switch(checked = uiState.reminderEnabled, onCheckedChange = viewModel::setReminderEnabled)
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
                    ListItem(headlineContent = { Text("Export to CSV") }, trailingContent = { Text("\u2192") })
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Backup") }, trailingContent = { Text("\u2192") })
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Restore from backup") }, trailingContent = { Text("\u2192") })
                    HorizontalDivider()
                    ListItem(headlineContent = { Text("Clear all data") }, trailingContent = { Text("\u2192", color = MaterialTheme.colorScheme.error) })
                }
            }

            // ABOUT
            Text("ABOUT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(headlineContent = { Text("SpendWise v1.0.0") })
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
