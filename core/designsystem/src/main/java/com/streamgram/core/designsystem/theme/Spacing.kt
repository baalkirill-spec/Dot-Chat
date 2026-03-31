package com.streamgram.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class StreamSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
)

val LocalStreamSpacing = staticCompositionLocalOf { StreamSpacing() }

/** Design-token access point. Use `StreamTheme.spacing` in composables. */
object StreamTheme {
    val spacing: StreamSpacing
        @Composable
        get() = LocalStreamSpacing.current
}
