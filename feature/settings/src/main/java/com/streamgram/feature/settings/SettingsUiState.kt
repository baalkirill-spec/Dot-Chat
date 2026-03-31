package com.streamgram.feature.settings

import com.streamgram.core.model.AppSettings
import com.streamgram.core.model.User

data class SettingsUiState(
    val settings: AppSettings = AppSettings(
        mediaAutoplayEnabled = true,
        channelRecommendationsEnabled = true,
        richAnimationsEnabled = true,
        selectedLanguageTag = "system",
        hasSeenOnboarding = true,
        hasAcceptedLegal = true,
        powerSavingModeEnabled = false,
        automaticPowerSavingEnabled = true,
    ),
    val currentUser: User? = null,
)
