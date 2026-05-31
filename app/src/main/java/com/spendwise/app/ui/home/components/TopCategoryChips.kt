package com.spendwise.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.CategorySpend
import com.spendwise.app.ui.components.categoryIconFor
import com.spendwise.app.ui.components.rememberCategoryColor
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
            val categoryColor = rememberCategoryColor(spend.category.colorHex)
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = categoryIconFor(spend.category.icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        "${spend.category.name} ${currencyFormat.format(spend.amount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = categoryColor.copy(alpha = 0.15f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    leadingIconContentColor = categoryColor
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = categoryColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}
