package com.spendwise.app.ui.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.PaymentMethod

@Composable
fun FilterChipsRow(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    selectedPaymentMethod: PaymentMethod?,
    onCategoryToggle: (Long) -> Unit,
    onPaymentMethodSelect: (PaymentMethod?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { cat ->
            FilterChip(
                selected = cat.id in selectedCategoryIds,
                onClick = { onCategoryToggle(cat.id) },
                label = { Text(cat.name) }
            )
        }
        item {
            FilterChip(
                selected = selectedPaymentMethod == PaymentMethod.UPI,
                onClick = { onPaymentMethodSelect(if (selectedPaymentMethod == PaymentMethod.UPI) null else PaymentMethod.UPI) },
                label = { Text("UPI") }
            )
        }
        item {
            FilterChip(
                selected = selectedPaymentMethod == PaymentMethod.CASH,
                onClick = { onPaymentMethodSelect(if (selectedPaymentMethod == PaymentMethod.CASH) null else PaymentMethod.CASH) },
                label = { Text("Cash") }
            )
        }
        item {
            FilterChip(
                selected = selectedPaymentMethod == PaymentMethod.CARD,
                onClick = { onPaymentMethodSelect(if (selectedPaymentMethod == PaymentMethod.CARD) null else PaymentMethod.CARD) },
                label = { Text("Card") }
            )
        }
    }
}
