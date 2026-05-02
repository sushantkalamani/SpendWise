package com.spendwise.app.ui.onboarding.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PulsingOrb(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerAlpha"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 4

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E676).copy(alpha = outerAlpha), Color.Transparent),
                center = center,
                radius = baseRadius * scale * 2
            ),
            radius = baseRadius * scale * 2,
            center = center
        )

        // Middle ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E676).copy(alpha = 0.4f), Color(0xFF00BFA5).copy(alpha = 0.1f)),
                center = center,
                radius = baseRadius * scale * 1.3f
            ),
            radius = baseRadius * scale * 1.3f,
            center = center
        )

        // Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E676), Color(0xFF00BFA5)),
                center = center,
                radius = baseRadius * scale
            ),
            radius = baseRadius * scale * 0.6f,
            center = center
        )
    }
}
