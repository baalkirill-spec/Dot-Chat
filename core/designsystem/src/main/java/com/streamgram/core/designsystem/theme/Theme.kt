package com.streamgram.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// ── Monochrome-first: black/white primary, blue as subtle accent ──────
private val DotChatDarkColors = darkColorScheme(
    primary = SurfaceWhite,
    onPrimary = Slate950,
    secondary = DotChatBlue,
    onSecondary = SurfaceWhite,
    tertiary = DotChatGreen,
    onTertiary = Slate950,
    error = DotChatRed,
    background = Slate950,
    surface = Slate900,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = Slate800,
    surfaceVariant = Slate800,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite,
    onSurfaceVariant = Slate300,
    outline = DividerDark,
    outlineVariant = WhiteAlpha12,
)

private val DotChatLightColors = lightColorScheme(
    primary = Slate950,
    onPrimary = SurfaceWhite,
    secondary = DotChatBlue,
    onSecondary = SurfaceWhite,
    tertiary = DotChatGreen,
    onTertiary = SurfaceWhite,
    error = DotChatRed,
    background = SurfaceWhite,
    surface = SurfaceWhite,
    surfaceContainer = SurfaceMuted,
    surfaceContainerHigh = SurfaceWhite,
    surfaceVariant = Slate100,
    onBackground = Slate950,
    onSurface = Slate950,
    onSurfaceVariant = Slate500,
    outline = DividerLight,
    outlineVariant = Slate100,
)

private val DotChatShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun DotChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalStreamSpacing provides StreamSpacing(),
        LocalDotChatMotionPreferences provides DotChatMotionPreferences(reducedMotionEnabled = reducedMotion),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DotChatDarkColors else DotChatLightColors,
            typography = DotChatTypography,
            shapes = DotChatShapes,
            content = content,
        )
    }
}

/** Backwards-compatible alias. Prefer [DotChatTheme]. */
@Composable
fun StreamGramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    DotChatTheme(
        darkTheme = darkTheme,
        reducedMotion = reducedMotion,
        content = content,
    )
}
