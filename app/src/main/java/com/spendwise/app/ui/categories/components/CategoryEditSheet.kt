package com.spendwise.app.ui.categories.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spendwise.app.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditSheet(
    existingCategory: Category?,
    onSave: (name: String, icon: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var icon by remember { mutableStateOf(existingCategory?.icon ?: "Restaurant") }
    var colorHex by remember { mutableStateOf(existingCategory?.colorHex ?: "#4CAF50") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (existingCategory != null) "Edit Category" else "Add Category", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Text("Icon", style = MaterialTheme.typography.labelLarge)
            IconPicker(selectedIcon = icon, onIconSelected = { icon = it }, modifier = Modifier.height(100.dp))

            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorPicker(selectedColor = colorHex, onColorSelected = { colorHex = it }, modifier = Modifier.height(100.dp))

            Button(onClick = { if (name.isNotBlank()) onSave(name, icon, colorHex) }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}
