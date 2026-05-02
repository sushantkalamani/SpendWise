package com.spendwise.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TopCategoryChips(
    categories: List<CategorySpend>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { spend ->
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        "${spend.category.name} ${currencyFormat.format(spend.amount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}
