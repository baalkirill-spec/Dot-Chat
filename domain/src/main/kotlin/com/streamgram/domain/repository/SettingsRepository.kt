package com.streamgram.domain.repository

import com.streamgram.core.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setMediaAutoplayEnabled(enabled: Boolean)
    suspend fun setChannelRecommendationsEnabled(enabled: Boolean)
    suspend fun setRichAnimationsEnabled(enabled: Boolean)
    suspend fun setLanguage(languageTag: String)
    suspend fun setLegalAccepted(accepted: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setPowerSavingModeEnabled(enabled: Boolean)
    suspend fun setAutomaticPowerSavingEnabled(enabled: Boolean)
}
