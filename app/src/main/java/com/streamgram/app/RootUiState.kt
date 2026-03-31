package com.streamgram.app

import com.streamgram.core.model.AuthState

data class RootUiState(
    val showSplash: Boolean = true,
    val authorizationState: AuthState = AuthState.Initial,
    val shouldShowOnboarding: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
)
