package com.example.financialliteracyapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = BgCard,
    secondary = Accent,
    onSecondary = BgCard,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    error = Danger,
    outline = Divider
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = BgCard,
    secondary = Accent,
    onSecondary = BgCard,
    background = BgPrimaryDark,
    onBackground = TextPrimaryDark,
    surface = BgCardDark,
    onSurface = TextPrimaryDark,
    error = Danger
)

@Composable
fun MiniEconomyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}