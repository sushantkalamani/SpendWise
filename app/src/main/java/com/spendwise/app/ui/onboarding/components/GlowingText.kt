package com.spendwise.app.ui.onboarding.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlowingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00E676),
    style: TextStyle = MaterialTheme.typography.displayMedium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Text(
        text = text,
        style = style.copy(
            color = color,
            shadow = Shadow(
                color = color.copy(alpha = glowAlpha),
                offset = Offset.Zero,
                blurRadius = 20f
            ),
            letterSpacing = 4.sp
        ),
        modifier = modifier
    )
}
