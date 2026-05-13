package com.spendwise.app.ui.history

import com.spendwise.app.domain.model.Category
import com.spendwise.app.domain.model.Expense
import com.spendwise.app.domain.model.PaymentMethod
import kotlinx.datetime.LocalDate

/**
 * UI state for the History screen.
 *
 * Tracks expenses, filter/sort state, and metadata like active
 * filter count and whether the user has any expenses at all.
 */
data class HistoryUiState(
    val searchQuery: String = "",
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true,

    // Date range filter
    val dateRangeStart: LocalDate? = null,
    val dateRangeEnd: LocalDate? = null,

    // Sort option
    val sortOption: SortOption = SortOption.NEWEST,

    // Filter metadata
    val activeFilterCount: Int = 0,

    // True when the user has zero expenses in the entire database
    val hasNoExpensesAtAll: Boolean = false
)

/** Available sort options for the history expense list. */
enum class SortOption(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    AMOUNT_HIGH("Amount: High → Low"),
    AMOUNT_LOW("Amount: Low → High")
}
