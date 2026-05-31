package com.spendwise.app.ui.categories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.ui.components.categoryIconFor

val availableIcons = listOf("Restaurant", "DirectionsCar", "Receipt", "ShoppingBag", "LocalHospital", "Movie", "ShoppingCart", "MoreHoriz", "Flight", "School", "Pets", "Bolt", "Checkroom", "SportsEsports")

@Composable
fun IconPicker(selectedIcon: String, onIconSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(availableIcons) { icon ->
            val isSelected = icon == selectedIcon
            FilledTonalIconButton(
                onClick = { onIconSelected(icon) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = categoryIconFor(icon),
                    contentDescription = icon,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
