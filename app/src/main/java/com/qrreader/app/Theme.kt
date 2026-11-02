package com.qrreader.app

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WhiteScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EFEA),
    onPrimaryContainer = Color(0xFF00201C),
    secondaryContainer = Color(0xFFE3F1EF),
    onSecondaryContainer = Color(0xFF0B2B27),
    background = Color.White,
    onBackground = Color(0xFF14181A),
    surface = Color.White,
    onSurface = Color(0xFF14181A),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5B6368),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8F9),
    surfaceContainer = Color(0xFFF3F5F6),
    surfaceContainerHigh = Color(0xFFEEF0F2),
    surfaceContainerHighest = Color(0xFFE8EBED),
    outline = Color(0xFFC9CED2),
    outlineVariant = Color(0xFFE3E6E8),
)

private val GreyScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFE0DB),
    onPrimaryContainer = Color(0xFF00201C),
    secondaryContainer = Color(0xFFC6D3D1),
    onSecondaryContainer = Color(0xFF0B2B27),
    background = Color(0xFFD9DCDF),
    onBackground = Color(0xFF1B1F23),
    surface = Color(0xFFD9DCDF),
    onSurface = Color(0xFF1B1F23),
    surfaceVariant = Color(0xFFCDD1D5),
    onSurfaceVariant = Color(0xFF4A5157),
    surfaceContainerLowest = Color(0xFFE9EBED),
    surfaceContainerLow = Color(0xFFE0E3E5),
    surfaceContainer = Color(0xFFE4E6E9),
    surfaceContainerHigh = Color(0xFFEBEDEF),
    surfaceContainerHighest = Color(0xFFF1F2F4),
    outline = Color(0xFFA9AFB4),
    outlineVariant = Color(0xFFC3C8CC),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF4FD1C5),
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF124B46),
    onPrimaryContainer = Color(0xFFC8F2EC),
    secondaryContainer = Color(0xFF1E3B38),
    onSecondaryContainer = Color(0xFFCDE9E5),
    background = Color(0xFF0F1114),
    onBackground = Color(0xFFE6E9EC),
    surface = Color(0xFF0F1114),
    onSurface = Color(0xFFE6E9EC),
    surfaceVariant = Color(0xFF262B30),
    onSurfaceVariant = Color(0xFF9AA3AB),
    surfaceContainerLowest = Color(0xFF0A0C0E),
    surfaceContainerLow = Color(0xFF14171A),
    surfaceContainer = Color(0xFF181B1F),
    surfaceContainerHigh = Color(0xFF1F2328),
    surfaceContainerHighest = Color(0xFF272C32),
    outline = Color(0xFF3C434A),
    outlineVariant = Color(0xFF2B3138),
)

/** Colours used for the little theme previews on the Settings screen. */
data class Swatch(val background: Color, val card: Color, val accent: Color, val text: Color)

fun swatchFor(theme: AppTheme): Swatch {
    val scheme = when (theme) {
        AppTheme.WHITE -> WhiteScheme
        AppTheme.GREY -> GreyScheme
        AppTheme.DARK -> DarkScheme
    }
    return Swatch(scheme.background, scheme.surfaceContainerHigh, scheme.primary, scheme.onSurfaceVariant)
}

@Composable
fun QRReaderTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val scheme = when (theme) {
        AppTheme.WHITE -> WhiteScheme
        AppTheme.GREY -> GreyScheme
        AppTheme.DARK -> DarkScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            val lightBars = theme != AppTheme.DARK
            controller.isAppearanceLightStatusBars = lightBars
            controller.isAppearanceLightNavigationBars = lightBars
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
