package com.spendwise.app.ui.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.PaymentMethod
import com.spendwise.app.ui.history.SortOption

/**
 * Horizontally scrollable row of filter chips for the History screen.
 *
 * Includes:
 * - Date range chip (opens a date range picker)
 * - Sort chip (cycles through sort options)
 * - Category filter chips (toggle individual categories)
 * - Payment method chips (UPI, Cash, Card)
 * - "Clear filters" chip (only visible when filters are active)
 *
 * @param activeFilterCount Number of non-default filters currently active.
 * @param onDateRangeClick Called when the date range chip is tapped.
 * @param onSortClick Called when the sort chip is tapped.
 * @param onClearFilters Called when the clear-filters chip is tapped.
 */
@Composable
fun FilterChipsRow(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    selectedPaymentMethod: PaymentMethod?,
    sortOption: SortOption,
    activeFilterCount: Int,
    hasDateRange: Boolean,
    onCategoryToggle: (Long) -> Unit,
    onPaymentMethodSelect: (PaymentMethod?) -> Unit,
    onDateRangeClick: () -> Unit,
    onSortClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Clear all filters (only if some are active)
        if (activeFilterCount > 0) {
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearFilters,
                    label = { Text("Clear ($activeFilterCount)") },
                    leadingIcon = { Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Date range
        item {
            FilterChip(
                selected = hasDateRange,
                onClick = onDateRangeClick,
                label = { Text("Date Range") },
                leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // Sort
        item {
            FilterChip(
                selected = sortOption != SortOption.NEWEST,
                onClick = onSortClick,
                label = { Text(sortOption.label) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // Category chips
        items(categories) { cat ->
            FilterChip(
                selected = cat.id in selectedCategoryIds,
                onClick = { onCategoryToggle(cat.id) },
                label = { Text(cat.name) }
            )
        }

        // Payment method chips
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
