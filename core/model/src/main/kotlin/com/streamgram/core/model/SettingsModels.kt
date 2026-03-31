package com.streamgram.core.model

data class AppSettings(
    val mediaAutoplayEnabled: Boolean,
    val channelRecommendationsEnabled: Boolean,
    val richAnimationsEnabled: Boolean,
    val selectedLanguageTag: String,
    val hasSeenOnboarding: Boolean,
    val hasAcceptedLegal: Boolean,
    val powerSavingModeEnabled: Boolean,
    val automaticPowerSavingEnabled: Boolean,
)
