package com.hiyako.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nexus AI is a dark-only experience, so the Deep Core scheme is applied
 * regardless of the system theme — the neon palette never gets washed out.
 */
private val DeepCoreColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpaceBlack,
    primaryContainer = NeonCyanMuted,
    onPrimaryContainer = DeepSpaceBlack,
    secondary = NeonMagenta,
    onSecondary = DeepSpaceBlack,
    secondaryContainer = NeonMagentaMuted,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonCyan,
    onTertiary = DeepSpaceBlack,
    background = DeepSpaceBlack,
    onBackground = TextPrimary,
    surface = DeepSpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepSpaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DeepSpaceOutline,
    outlineVariant = DeepSpaceOutline,
    error = NeonError,
    onError = DeepSpaceBlack
)

private val NexusTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
)

@Composable
fun NexusAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeepCoreColorScheme,
        typography = NexusTypography,
        content = content
    )
}
