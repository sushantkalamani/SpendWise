package com.spendwise.app.ui.addexpense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.ui.components.AmountInput
import com.spendwise.app.ui.components.CategoryChipGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDetailScreen(
    viewModel: AddExpenseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate)
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismiss()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            AmountInput(
                amount = uiState.amount,
                onAmountChange = viewModel::updateAmount,
                isError = uiState.amountError
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            CategoryChipGrid(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                isError = uiState.categoryError
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker
            OutlinedTextField(
                value = remember(uiState.selectedDate) {
                    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(uiState.selectedDate)
                    val dt = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                    "${dt.dayOfMonth}/${dt.monthNumber}/${dt.year}"
                },
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also { source ->
                    LaunchedEffect(source) {
                        source.interactions.collect { interaction ->
                            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                showDatePicker = true
                            }
                        }
                    }
                }
            )

            // Payment method
            Text("Payment Method", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val methods = listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD)
                methods.forEachIndexed { index, method ->
                    SegmentedButton(
                        selected = uiState.paymentMethod == method,
                        onClick = { viewModel.updatePaymentMethod(method) },
                        shape = SegmentedButtonDefaults.itemShape(index, methods.size)
                    ) {
                        Text(method.name)
                    }
                }
            }

            // Tags
            Text("Tags", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.newTagText,
                    onValueChange = viewModel::updateNewTagText,
                    label = { Text("Add tag") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(
                    onClick = { viewModel.addTag(uiState.newTagText) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Add")
                }
            }
            if (uiState.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = { Text("\u2715", style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::saveExpense,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Expense")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
