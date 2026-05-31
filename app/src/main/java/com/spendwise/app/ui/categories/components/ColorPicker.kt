package com.spendwise.app.ui.categories.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val availableColors = listOf("#4CAF50","#2196F3","#FF9800","#E91E63","#F44336","#9C27B0","#8BC34A","#607D8B","#00BCD4","#FFEB3B","#795548","#3F51B5")

@Composable
fun ColorPicker(selectedColor: String, onColorSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(columns = GridCells.Fixed(8), modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(availableColors) { hex ->
            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
            val isSelected = hex == selectedColor
            val selectedRing = MaterialTheme.colorScheme.onSurface
            Canvas(modifier = Modifier.size(36.dp).clickable { onColorSelected(hex) }) {
                drawCircle(color = color, radius = size.minDimension / 2)
                if (isSelected) drawCircle(color = selectedRing, radius = size.minDimension / 3, style = Stroke(3.dp.toPx()))
            }
        }
    }
}
