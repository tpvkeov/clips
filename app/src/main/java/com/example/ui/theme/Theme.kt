package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ClipsTheme(
    accentTheme: AppThemeAccent = AppThemeAccent.ORANGE_GREEN,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = buildColorScheme(accent = accentTheme, darkTheme = darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClipsTypography,
        content = content
    )
}
