package com.spendwise.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5F4E7),
    onPrimaryContainer = Color(0xFF06210A),
    secondary = Blue40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5F2FF),
    onSecondaryContainer = Color(0xFF031E36),
    tertiary = Orange40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE8CC),
    onTertiaryContainer = Color(0xFF2A1600),
    error = Red40,
    background = Color(0xFFFAFCFA),
    onBackground = Color(0xFF181D19),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181D19),
    surfaceVariant = Color(0xFFEFF3EF),
    onSurfaceVariant = Color(0xFF535D55),
    outline = Color(0xFF737D75),
    outlineVariant = Color(0xFFD6DED7)
)

private val MatteColorScheme = darkColorScheme(
    primary = MatteMint,
    onPrimary = MatteBlack,
    primaryContainer = MatteMintContainer,
    onPrimaryContainer = MatteMint,
    secondary = MatteCyan,
    onSecondary = MatteBlack,
    secondaryContainer = MatteCyanContainer,
    onSecondaryContainer = MatteCyan,
    tertiary = MatteAmber,
    onTertiary = MatteBlack,
    tertiaryContainer = MatteAmberContainer,
    onTertiaryContainer = MatteAmber,
    error = MatteCoral,
    onError = MatteBlack,
    errorContainer = MatteCoralContainer,
    onErrorContainer = MatteCoral,
    background = MatteBlack,
    onBackground = MatteText,
    surface = MatteSurface,
    onSurface = MatteText,
    surfaceVariant = MatteSurfaceVariant,
    onSurfaceVariant = MatteTextMuted,
    outline = MatteOutline,
    outlineVariant = MatteOutlineSoft,
    inverseSurface = MatteText,
    inverseOnSurface = MatteBlack,
    scrim = Color(0xCC000000),
    surfaceTint = MatteMint
)

@Composable
fun SpendWiseTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && !darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        darkTheme -> MatteColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendWiseTypography,
        content = content
    )
}
