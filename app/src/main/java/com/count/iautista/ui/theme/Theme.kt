package com.count.iautista.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val IautistaColorScheme = lightColorScheme(
    primary              = ColorPrimary,
    onPrimary            = ColorOnPrimary,
    primaryContainer     = ColorPrimaryContainer,
    onPrimaryContainer   = ColorOnPrimaryContainer,
    secondary            = ColorSecondary,
    onSecondary          = ColorOnSecondary,
    secondaryContainer   = ColorSecondaryContainer,
    onSecondaryContainer = ColorOnSecondaryContainer,
    tertiary             = ColorTertiary,
    onTertiary           = ColorOnTertiary,
    tertiaryContainer    = ColorTertiaryContainer,
    onTertiaryContainer  = ColorOnTertiaryContainer,
    background           = ColorBackground,
    surface              = ColorSurface,
    surfaceVariant       = ColorSurfaceVariant,
    onBackground         = ColorTextPrimary,
    onSurface            = ColorTextPrimary,
    onSurfaceVariant     = ColorTextSecondary,
    error                = ColorError,
    errorContainer       = ColorErrorContainer,
    outline              = ColorNeutral,
    outlineVariant       = ColorOutlineVariant,
)

@Composable
fun IautistaTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = IautistaColorScheme,
            typography  = IautistaTypography,
            shapes      = IautistaShapes,
            content     = content,
        )
    }
}
