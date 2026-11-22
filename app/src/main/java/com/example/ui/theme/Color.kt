package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeAccent(
    val id: String,
    val labelAr: String,
    val swatchColor: Color,
    val secondarySwatch: Color
) {
    ORANGE_GREEN(
        id = "orange_green",
        labelAr = "برتقالي أخضر",
        swatchColor = Color(0xFFFF6D00),
        secondarySwatch = Color(0xFF10B981)
    ),
    EMERALD_GREEN(
        id = "emerald_green",
        labelAr = "أخضر زمردي",
        swatchColor = Color(0xFF10B981),
        secondarySwatch = Color(0xFFFF8F00)
    ),
    ROYAL_BLUE(
        id = "royal_blue",
        labelAr = "أزرق ملكي",
        swatchColor = Color(0xFF3B82F6),
        secondarySwatch = Color(0xFF10B981)
    ),
    VIOLET(
        id = "violet",
        labelAr = "بنفسجي",
        swatchColor = Color(0xFFA855F7),
        secondarySwatch = Color(0xFFFF8A65)
    ),
    CRIMSON(
        id = "crimson",
        labelAr = "قرمزي",
        swatchColor = Color(0xFFFF3B5C),
        secondarySwatch = Color(0xFF10B981)
    )
}

enum class ButtonColorTarget(val id: String, val labelAr: String) {
    HOME_TAB("home", "الرئيسية"),
    CLIPS_TAB("clips", "clips"),
    UPLOAD_BUTTON("upload", "علامة الرفع"),
    POSTS_TAB("posts", "المنشورات"),
    PROFILE_TAB("profile", "حسابي"),
    ACTION_BUTTONS("actions", "أزرار التفاعل")
}

@Immutable
data class CustomButtonColors(
    val homeTabColor: Color? = null,
    val clipsTabColor: Color? = null,
    val uploadButtonColor: Color? = null,
    val postsTabColor: Color? = null,
    val profileTabColor: Color? = null,
    val actionButtonsColor: Color? = null
) {
    fun colorFor(target: ButtonColorTarget): Color? = when (target) {
        ButtonColorTarget.HOME_TAB -> homeTabColor
        ButtonColorTarget.CLIPS_TAB -> clipsTabColor
        ButtonColorTarget.UPLOAD_BUTTON -> uploadButtonColor
        ButtonColorTarget.POSTS_TAB -> postsTabColor
        ButtonColorTarget.PROFILE_TAB -> profileTabColor
        ButtonColorTarget.ACTION_BUTTONS -> actionButtonsColor
    }

    fun withColor(target: ButtonColorTarget, color: Color?): CustomButtonColors = when (target) {
        ButtonColorTarget.HOME_TAB -> copy(homeTabColor = color)
        ButtonColorTarget.CLIPS_TAB -> copy(clipsTabColor = color)
        ButtonColorTarget.UPLOAD_BUTTON -> copy(uploadButtonColor = color)
        ButtonColorTarget.POSTS_TAB -> copy(postsTabColor = color)
        ButtonColorTarget.PROFILE_TAB -> copy(profileTabColor = color)
        ButtonColorTarget.ACTION_BUTTONS -> copy(actionButtonsColor = color)
    }

    fun hasAnyCustomColor(): Boolean =
        homeTabColor != null ||
            clipsTabColor != null ||
            uploadButtonColor != null ||
            postsTabColor != null ||
            profileTabColor != null ||
            actionButtonsColor != null
}

val LocalCustomButtonColors = compositionLocalOf { CustomButtonColors() }

val ButtonSwatchPalette: List<Pair<String, Color>> = listOf(
    "برتقالي" to Color(0xFFFF7A1A),
    "أخضر" to Color(0xFF10B981),
    "أزرق" to Color(0xFF3B82F6),
    "بنفسجي" to Color(0xFFA855F7),
    "قرمزي" to Color(0xFFFF3B5C),
    "ذهبي" to Color(0xFFF59E0B),
    "سماوي" to Color(0xFF06B6D4),
    "وردي" to Color(0xFFEC4899)
)

private val DarkBackground = Color(0xFF0E131B)
private val DarkSurface = Color(0xFF141A24)
private val DarkSurfaceVariant = Color(0xFF1D2634)
private val DarkSurfaceContainer = Color(0xFF18202C)
private val DarkSurfaceContainerHigh = Color(0xFF222D3D)
private val DarkOnSurface = Color(0xFFF3F6FA)
private val DarkOnSurfaceVariant = Color(0xFFAAB4C4)
private val DarkOutline = Color(0xFF313D50)

private val LightBackground = Color(0xFFF6F8FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFEAF0F6)
private val LightSurfaceContainer = Color(0xFFF1F5F9)
private val LightSurfaceContainerHigh = Color(0xFFE4ECF4)
private val LightOnSurface = Color(0xFF121822)
private val LightOnSurfaceVariant = Color(0xFF526072)
private val LightOutline = Color(0xFFCFD8E3)

fun buildColorScheme(accent: AppThemeAccent, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        when (accent) {
            AppThemeAccent.ORANGE_GREEN -> darkColorScheme(
                primary = Color(0xFFFF7A1A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF542504),
                onPrimaryContainer = Color(0xFFFFE0C7),
                secondary = Color(0xFF10B981),
                onSecondary = Color(0xFF03261A),
                secondaryContainer = Color(0xFF0B4633),
                onSecondaryContainer = Color(0xFFC2FCE5),
                tertiary = Color(0xFFFFB300),
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                surfaceContainer = DarkSurfaceContainer,
                surfaceContainerHigh = DarkSurfaceContainerHigh,
                outline = DarkOutline
            )
            AppThemeAccent.EMERALD_GREEN -> darkColorScheme(
                primary = Color(0xFF10B981),
                onPrimary = Color(0xFF032318),
                primaryContainer = Color(0xFF0D4432),
                onPrimaryContainer = Color(0xFFB8F7DF),
                secondary = Color(0xFFFF8F00),
                onSecondary = Color(0xFF2D1600),
                secondaryContainer = Color(0xFF522905),
                onSecondaryContainer = Color(0xFFFFE0B2),
                tertiary = Color(0xFF34D399),
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                surfaceContainer = DarkSurfaceContainer,
                surfaceContainerHigh = DarkSurfaceContainerHigh,
                outline = DarkOutline
            )
            AppThemeAccent.ROYAL_BLUE -> darkColorScheme(
                primary = Color(0xFF4D94FF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF132E5C),
                onPrimaryContainer = Color(0xFFD6E4FF),
                secondary = Color(0xFF10B981),
                onSecondary = Color(0xFF032318),
                secondaryContainer = Color(0xFF0D4432),
                onSecondaryContainer = Color(0xFFB8F7DF),
                tertiary = Color(0xFFFF8F00),
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                surfaceContainer = DarkSurfaceContainer,
                surfaceContainerHigh = DarkSurfaceContainerHigh,
                outline = DarkOutline
            )
            AppThemeAccent.VIOLET -> darkColorScheme(
                primary = Color(0xFFB36BFF),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF3E1866),
                onPrimaryContainer = Color(0xFFF0DBFF),
                secondary = Color(0xFFFF8A65),
                onSecondary = Color(0xFF3B1205),
                secondaryContainer = Color(0xFF571E0C),
                onSecondaryContainer = Color(0xFFFFDBD0),
                tertiary = Color(0xFF10B981),
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                surfaceContainer = DarkSurfaceContainer,
                surfaceContainerHigh = DarkSurfaceContainerHigh,
                outline = DarkOutline
            )
            AppThemeAccent.CRIMSON -> darkColorScheme(
                primary = Color(0xFFFF3B5C),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF5C1122),
                onPrimaryContainer = Color(0xFFFFD9DF),
                secondary = Color(0xFF10B981),
                onSecondary = Color(0xFF032318),
                secondaryContainer = Color(0xFF0D4432),
                onSecondaryContainer = Color(0xFFB8F7DF),
                tertiary = Color(0xFFFF8A65),
                background = DarkBackground,
                onBackground = DarkOnSurface,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                surfaceContainer = DarkSurfaceContainer,
                surfaceContainerHigh = DarkSurfaceContainerHigh,
                outline = DarkOutline
            )
        }
    } else {
        when (accent) {
            AppThemeAccent.ORANGE_GREEN -> lightColorScheme(
                primary = Color(0xFFEA580C),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFEDD5),
                onPrimaryContainer = Color(0xFF7C2D12),
                secondary = Color(0xFF059669),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD1FAE5),
                onSecondaryContainer = Color(0xFF064E3B),
                tertiary = Color(0xFFD97706),
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                surfaceContainer = LightSurfaceContainer,
                surfaceContainerHigh = LightSurfaceContainerHigh,
                outline = LightOutline
            )
            AppThemeAccent.EMERALD_GREEN -> lightColorScheme(
                primary = Color(0xFF059669),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1FAE5),
                onPrimaryContainer = Color(0xFF064E3B),
                secondary = Color(0xFFEA580C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEDD5),
                onSecondaryContainer = Color(0xFF7C2D12),
                tertiary = Color(0xFF10B981),
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                surfaceContainer = LightSurfaceContainer,
                surfaceContainerHigh = LightSurfaceContainerHigh,
                outline = LightOutline
            )
            AppThemeAccent.ROYAL_BLUE -> lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDBEAFE),
                onPrimaryContainer = Color(0xFF1E3A8A),
                secondary = Color(0xFF059669),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD1FAE5),
                onSecondaryContainer = Color(0xFF064E3B),
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                surfaceContainer = LightSurfaceContainer,
                surfaceContainerHigh = LightSurfaceContainerHigh,
                outline = LightOutline
            )
            AppThemeAccent.VIOLET -> lightColorScheme(
                primary = Color(0xFF7C3AED),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEDE9FE),
                onPrimaryContainer = Color(0xFF3B0764),
                secondary = Color(0xFFEA580C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEDD5),
                onSecondaryContainer = Color(0xFF7C2D12),
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                surfaceContainer = LightSurfaceContainer,
                surfaceContainerHigh = LightSurfaceContainerHigh,
                outline = LightOutline
            )
            AppThemeAccent.CRIMSON -> lightColorScheme(
                primary = Color(0xFFE11D48),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFE0E6),
                onPrimaryContainer = Color(0xFF4C0519),
                secondary = Color(0xFF059669),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD1FAE5),
                onSecondaryContainer = Color(0xFF064E3B),
                background = LightBackground,
                onBackground = LightOnSurface,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                surfaceContainer = LightSurfaceContainer,
                surfaceContainerHigh = LightSurfaceContainerHigh,
                outline = LightOutline
            )
        }
    }
}
