package com.therapist.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Legacy aliases used across UI kit
val Rose50 = Color(0xFFFFF5F6)
val Rose100 = Color(0xFFFFE4E8)
val Rose200 = Color(0xFFFFC9D1)
val Rose400 = Color(0xFFE63950)
val Rose500 = Color(0xFFC41E3A)
val Rose700 = Color(0xFF8B1530)
val Rose900 = Color(0xFF4A0E1C)

val Emerald500 = Color(0xFF059669)
val Emerald100 = Color(0xFFD1FAE5)
val Amber500 = Color(0xFFD97706)
val Amber100 = Color(0xFFFEF3C7)
val Sky500 = Color(0xFF0284C7)
val Sky100 = Color(0xFFE0F2FE)
val Sky50 = Color(0xFFF0F9FF)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate100 = Color(0xFFF1F5F9)

enum class AppColorTheme(val id: String, val titleFa: String) {
    SKY("sky", "آبی آسمان"),
    ROSE("rose", "رز سرخ"),
    EMERALD("emerald", "زمردی"),
    VIOLET("violet", "بنفش آرام"),
    SUNSET("sunset", "غروب طلایی");

    companion object {
        fun fromId(id: String?): AppColorTheme =
            entries.find { it.id == id } ?: SKY
    }
}

private fun lightScheme(
    primary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    background: Color,
    surfaceVariant: Color,
    outline: Color,
    secondary: Color = Emerald500,
    secondaryContainer: Color = Emerald100,
    tertiary: Color = Sky500,
    tertiaryContainer: Color = Sky100
) = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = tertiary,
    onTertiary = Color.White,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = background,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Slate500,
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.5f),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

private fun darkScheme(
    primary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color
) = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = Color(0xFF6EE7B7),
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Emerald100,
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF0C4A6E),
    tertiaryContainer = Color(0xFF0369A1),
    onTertiaryContainer = Sky100,
    background = background,
    onBackground = Color(0xFFF1F5F9),
    surface = surface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2)
)

fun colorSchemeFor(theme: AppColorTheme, dark: Boolean): ColorScheme {
    if (dark) {
        return when (theme) {
            AppColorTheme.SKY -> darkScheme(
                primary = Color(0xFF7DD3FC),
                primaryContainer = Color(0xFF0369A1),
                onPrimaryContainer = Color(0xFFE0F2FE),
                background = Color(0xFF0B1220),
                surface = Color(0xFF111827),
                surfaceVariant = Color(0xFF1E293B)
            )
            AppColorTheme.ROSE -> darkScheme(
                primary = Rose200,
                primaryContainer = Rose700,
                onPrimaryContainer = Rose100,
                background = Color(0xFF1A1214),
                surface = Color(0xFF241A1D),
                surfaceVariant = Color(0xFF3D2A30)
            )
            AppColorTheme.EMERALD -> darkScheme(
                primary = Color(0xFF6EE7B7),
                primaryContainer = Color(0xFF065F46),
                onPrimaryContainer = Emerald100,
                background = Color(0xFF0B1410),
                surface = Color(0xFF12201A),
                surfaceVariant = Color(0xFF1E3329)
            )
            AppColorTheme.VIOLET -> darkScheme(
                primary = Color(0xFFC4B5FD),
                primaryContainer = Color(0xFF5B21B6),
                onPrimaryContainer = Color(0xFFEDE9FE),
                background = Color(0xFF120F1C),
                surface = Color(0xFF1A1628),
                surfaceVariant = Color(0xFF2E2640)
            )
            AppColorTheme.SUNSET -> darkScheme(
                primary = Color(0xFFFDBA74),
                primaryContainer = Color(0xFFC2410C),
                onPrimaryContainer = Color(0xFFFFEDD5),
                background = Color(0xFF1A120C),
                surface = Color(0xFF241810),
                surfaceVariant = Color(0xFF3D2A1C)
            )
        }
    }
    return when (theme) {
        AppColorTheme.SKY -> lightScheme(
            primary = Color(0xFF0284C7),
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0C4A6E),
            background = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFFE0F2FE),
            outline = Color(0xFFBAE6FD),
            secondary = Color(0xFF0EA5E9),
            secondaryContainer = Color(0xFFBAE6FD),
            tertiary = Color(0xFF6366F1),
            tertiaryContainer = Color(0xFFE0E7FF)
        )
        AppColorTheme.ROSE -> lightScheme(
            primary = Rose500,
            primaryContainer = Rose100,
            onPrimaryContainer = Rose900,
            background = Rose50,
            surfaceVariant = Color(0xFFF8EDEF),
            outline = Color(0xFFE7D0D4)
        )
        AppColorTheme.EMERALD -> lightScheme(
            primary = Color(0xFF059669),
            primaryContainer = Emerald100,
            onPrimaryContainer = Color(0xFF064E3B),
            background = Color(0xFFF0FDF4),
            surfaceVariant = Color(0xFFDCFCE7),
            outline = Color(0xFFA7F3D0),
            secondary = Color(0xFF0D9488),
            secondaryContainer = Color(0xFFCCFBF1)
        )
        AppColorTheme.VIOLET -> lightScheme(
            primary = Color(0xFF7C3AED),
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF4C1D95),
            background = Color(0xFFF5F3FF),
            surfaceVariant = Color(0xFFEDE9FE),
            outline = Color(0xFFDDD6FE),
            secondary = Color(0xFF8B5CF6),
            secondaryContainer = Color(0xFFF3E8FF)
        )
        AppColorTheme.SUNSET -> lightScheme(
            primary = Color(0xFFEA580C),
            primaryContainer = Color(0xFFFFEDD5),
            onPrimaryContainer = Color(0xFF7C2D12),
            background = Color(0xFFFFF7ED),
            surfaceVariant = Color(0xFFFFEDD5),
            outline = Color(0xFFFED7AA),
            secondary = Color(0xFFF59E0B),
            secondaryContainer = Color(0xFFFEF3C7)
        )
    }
}

/** گرادیان هدر بر اساس تم */
fun headerGradientColors(theme: AppColorTheme, dark: Boolean): List<Color> {
    if (dark) {
        return when (theme) {
            AppColorTheme.SKY -> listOf(Color(0xFF0369A1), Color(0xFF0C4A6E))
            AppColorTheme.ROSE -> listOf(Rose500, Rose700)
            AppColorTheme.EMERALD -> listOf(Color(0xFF059669), Color(0xFF065F46))
            AppColorTheme.VIOLET -> listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))
            AppColorTheme.SUNSET -> listOf(Color(0xFFEA580C), Color(0xFFC2410C))
        }
    }
    return when (theme) {
        AppColorTheme.SKY -> listOf(Color(0xFF0EA5E9), Color(0xFF0284C7), Color(0xFF0369A1))
        AppColorTheme.ROSE -> listOf(Rose400, Rose500, Rose700)
        AppColorTheme.EMERALD -> listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF047857))
        AppColorTheme.VIOLET -> listOf(Color(0xFFA78BFA), Color(0xFF7C3AED), Color(0xFF6D28D9))
        AppColorTheme.SUNSET -> listOf(Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFFC2410C))
    }
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TherapistAssistantTheme(
    themeId: String = AppColorTheme.SKY.id,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val theme = AppColorTheme.fromId(themeId)
    val colorScheme = colorSchemeFor(theme, darkTheme)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
