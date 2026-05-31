package com.spendwise.app.ui.onboarding.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    neonColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = neonColor,
            contentColor = Color.Black
        ),
        modifier = modifier.width(220.dp).height(56.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
