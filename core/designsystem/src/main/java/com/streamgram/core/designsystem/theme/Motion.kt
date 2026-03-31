package com.streamgram.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf

data class DotChatMotionPreferences(
    val reducedMotionEnabled: Boolean = false,
)

val LocalDotChatMotionPreferences = compositionLocalOf { DotChatMotionPreferences() }
