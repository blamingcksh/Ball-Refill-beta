package com.neuro.minimalistlauncher.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MinimalDarkColorScheme = darkColorScheme(
    primary = PrimaryOffWhite,
    onPrimary = PureBlack,
    primaryContainer = CharcoalSurface,
    onPrimaryContainer = PrimaryOffWhite,
    secondary = SecondaryMutedAsh,
    onSecondary = PureBlack,
    secondaryContainer = CharcoalElevated,
    onSecondaryContainer = PrimaryOffWhite,
    tertiary = SecondaryMutedAsh,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = PrimaryOffWhite,
    surface = CharcoalSurface,
    onSurface = PrimaryOffWhite,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = SecondaryMutedAsh,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = SecondaryMutedAsh,
    onError = PureBlack
)

@Composable
fun NeuroMinimalLauncherTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MinimalDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = PureBlack.toArgb()
            window.navigationBarColor = PureBlack.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MinimalTypography,
        content = content
    )
}
