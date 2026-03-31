package com.streamgram.domain.usecase

import com.streamgram.core.model.PrivacySettings
import com.streamgram.domain.repository.PrivacyRepository
import javax.inject.Inject

class ObservePrivacySettingsUseCase @Inject constructor(
    private val repository: PrivacyRepository,
) {
    operator fun invoke() = repository.observePrivacySettings()
}

class ObserveBlockedUsersUseCase @Inject constructor(
    private val repository: PrivacyRepository,
) {
    operator fun invoke() = repository.observeBlockedUsers()
}

class UpdatePrivacySettingsUseCase @Inject constructor(
    private val repository: PrivacyRepository,
) {
    suspend operator fun invoke(settings: PrivacySettings) = repository.updatePrivacySettings(settings)
}
