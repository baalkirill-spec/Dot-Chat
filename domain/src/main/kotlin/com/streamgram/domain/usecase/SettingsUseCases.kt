package com.streamgram.domain.usecase

import com.streamgram.core.model.AppSettings
import com.streamgram.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}

class SetAppLanguageUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(languageTag: String) = repository.setLanguage(languageTag)
}

class SetLegalAcceptedUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(accepted: Boolean) = repository.setLegalAccepted(accepted)
}

class SetOnboardingCompletedUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(completed: Boolean) = repository.setOnboardingCompleted(completed)
}

class SetMediaAutoplayEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setMediaAutoplayEnabled(enabled)
}

class SetChannelRecommendationsEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setChannelRecommendationsEnabled(enabled)
}

class SetRichAnimationsEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setRichAnimationsEnabled(enabled)
}

class SetPowerSavingModeEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setPowerSavingModeEnabled(enabled)
}

class SetAutomaticPowerSavingEnabledUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAutomaticPowerSavingEnabled(enabled)
}
