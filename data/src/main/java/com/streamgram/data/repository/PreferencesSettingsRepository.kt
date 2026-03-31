package com.streamgram.data.repository

import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.model.AppSettings
import com.streamgram.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PreferencesSettingsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> {
        return preferences.preferences.map { snapshot ->
            AppSettings(
                mediaAutoplayEnabled = snapshot.mediaAutoplayEnabled,
                channelRecommendationsEnabled = snapshot.channelRecommendationsEnabled,
                richAnimationsEnabled = snapshot.richAnimationsEnabled,
                selectedLanguageTag = snapshot.selectedLanguageTag,
                hasSeenOnboarding = snapshot.hasSeenOnboarding,
                hasAcceptedLegal = snapshot.hasAcceptedLegal,
                powerSavingModeEnabled = snapshot.powerSavingModeEnabled,
                automaticPowerSavingEnabled = snapshot.automaticPowerSavingEnabled,
            )
        }
    }

    override suspend fun setMediaAutoplayEnabled(enabled: Boolean) {
        preferences.setMediaAutoplayEnabled(enabled)
    }

    override suspend fun setChannelRecommendationsEnabled(enabled: Boolean) {
        preferences.setChannelRecommendationsEnabled(enabled)
    }

    override suspend fun setRichAnimationsEnabled(enabled: Boolean) {
        preferences.setRichAnimationsEnabled(enabled)
    }

    override suspend fun setLanguage(languageTag: String) {
        preferences.setSelectedLanguageTag(languageTag)
    }

    override suspend fun setLegalAccepted(accepted: Boolean) {
        preferences.setHasAcceptedLegal(accepted)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.setHasSeenOnboarding(completed)
    }

    override suspend fun setPowerSavingModeEnabled(enabled: Boolean) {
        preferences.setPowerSavingModeEnabled(enabled)
    }

    override suspend fun setAutomaticPowerSavingEnabled(enabled: Boolean) {
        preferences.setAutomaticPowerSavingEnabled(enabled)
    }
}
