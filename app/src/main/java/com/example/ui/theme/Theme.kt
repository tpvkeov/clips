package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ClipsTheme(
    accentTheme: AppThemeAccent = AppThemeAccent.ORANGE_GREEN,
    darkTheme: Boolean = true,
    customButtonColors: CustomButtonColors = CustomButtonColors(),
    content: @Composable () -> Unit
) {
    val colorScheme = buildColorScheme(accent = accentTheme, darkTheme = darkTheme)
    CompositionLocalProvider(LocalCustomButtonColors provides customButtonColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ClipsTypography,
            content = content
        )
    }
}
