package com.spendwise.app.ui.onboarding.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00E676)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonBtn")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(2.dp, neonColor.copy(alpha = borderAlpha)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = neonColor
        ),
        modifier = modifier.width(220.dp).height(56.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
