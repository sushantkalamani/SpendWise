package com.spendwise.app.ui.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.ui.components.AmountInput
import com.spendwise.app.ui.components.CategoryChipGrid
import com.spendwise.app.ui.components.SectionHeader
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Full-screen form for editing an existing expense.
 *
 * Reuses the same layout as [AddExpenseDetailScreen] but operates in
 * edit mode — the title says "Edit Expense" and save calls update.
 *
 * @param viewModel Shared [AddExpenseViewModel] — caller must call
 *   [AddExpenseViewModel.loadExpenseForEdit] before showing this screen.
 * @param onDismiss Called when the user closes the screen (back or after save).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
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

            SectionHeader("Category")
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
                    val instant = Instant.fromEpochMilliseconds(uiState.selectedDate)
                    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
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
            SectionHeader("Payment Method", icon = Icons.Filled.CreditCard)
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
            SectionHeader("Tags", icon = Icons.AutoMirrored.Filled.Label)
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (uiState.isEditMode) "Update Expense" else "Save Expense")
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
