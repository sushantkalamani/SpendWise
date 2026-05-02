package com.spendwise.app.ui.addexpense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.components.AmountInput
import com.spendwise.app.ui.components.CategoryChipGrid
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    viewModel: AddExpenseViewModel,
    onDismiss: () -> Unit,
    onExpandToDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismiss()
            viewModel.resetState()
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            viewModel.resetState()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.titleMedium
            )

            AmountInput(
                amount = uiState.amount,
                onAmountChange = viewModel::updateAmount,
                isError = uiState.amountError,
                modifier = Modifier.focusRequester(focusRequester)
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)

            CategoryChipGrid(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                isError = uiState.categoryError
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onExpandToDetail) {
                    Text("Detailed \u2192")
                }

                Button(
                    onClick = viewModel::saveExpense,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}
