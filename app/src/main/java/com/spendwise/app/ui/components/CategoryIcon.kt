package com.spendwise.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun categoryIconFor(iconName: String?): ImageVector {
    return when (iconName) {
        "Restaurant" -> Icons.Filled.Restaurant
        "DirectionsCar" -> Icons.Filled.DirectionsCar
        "Receipt" -> Icons.Filled.Receipt
        "ShoppingBag" -> Icons.Filled.ShoppingBag
        "LocalHospital" -> Icons.Filled.LocalHospital
        "Movie" -> Icons.Filled.Movie
        "ShoppingCart" -> Icons.Filled.ShoppingCart
        "Flight" -> Icons.Filled.Flight
        "School" -> Icons.Filled.School
        "Pets" -> Icons.Filled.Pets
        "Bolt" -> Icons.Filled.Bolt
        "Checkroom" -> Icons.Filled.Checkroom
        "SportsEsports" -> Icons.Filled.SportsEsports
        else -> Icons.Filled.MoreHoriz
    }
}

@Composable
fun rememberCategoryColor(colorHex: String?): Color {
    val fallback = MaterialTheme.colorScheme.primary
    return remember(colorHex, fallback) {
        try {
            colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String?,
    contentDescription: String?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    Surface(
        color = color.copy(alpha = 0.16f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = categoryIconFor(iconName),
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
