package com.spendwise.app.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Category

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChipGrid(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    FlowRow(modifier = modifier) {
        categories.forEach { category ->
            val isSelected = selectedCategory?.id == category.id
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                leadingIcon = if (isSelected) {
                    { Text("\u2713") }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = try {
                        Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.2f)
                    } catch (_: Exception) {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
            )
        }
    }
    if (isError) {
        Text(
            "Select a category",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
