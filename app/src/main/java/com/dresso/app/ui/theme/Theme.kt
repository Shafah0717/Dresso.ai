package com.dresso.app.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DressoColorScheme = darkColorScheme(
    primary = DressoColors.Ivory,
    onPrimary = DressoColors.Ink,
    primaryContainer = DressoColors.PanelRaised,
    onPrimaryContainer = DressoColors.White,
    secondary = DressoColors.Muted,
    onSecondary = DressoColors.Ink,
    secondaryContainer = DressoColors.Panel,
    onSecondaryContainer = DressoColors.White,
    tertiary = DressoColors.Muted,
    onTertiary = DressoColors.Ink,
    background = DressoColors.Ink,
    onBackground = DressoColors.White,
    surface = DressoColors.Ink,
    onSurface = DressoColors.White,
    surfaceVariant = DressoColors.Panel,
    onSurfaceVariant = DressoColors.Muted,
    outline = DressoColors.Hairline,
    outlineVariant = DressoColors.HairlineSoft,
    error = Color(0xFFE3E3E3),
    onError = DressoColors.Ink,
    scrim = Color(0xCC000000),
)

private val DressoShapes = Shapes(
    extraSmall = RoundedCornerShape(1.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(2.dp),
)

@Composable
fun DressoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DressoColorScheme,
        typography = DressoTypography,
        shapes = DressoShapes,
        content = content,
    )
}

/**
 * Product pages are intentionally light; every other surface is dark.
 */
@Composable
fun SystemBarAppearance(useDarkIcons: Boolean) {
    val view = LocalView.current
    val activity = LocalActivity.current
    DisposableEffect(useDarkIcons, activity) {
        val window = activity?.window
        if (window == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(window, view)
            val previousStatus = controller.isAppearanceLightStatusBars
            val previousNavigation = controller.isAppearanceLightNavigationBars
            controller.isAppearanceLightStatusBars = useDarkIcons
            controller.isAppearanceLightNavigationBars = useDarkIcons
            onDispose {
                controller.isAppearanceLightStatusBars = previousStatus
                controller.isAppearanceLightNavigationBars = previousNavigation
            }
        }
    }
}
