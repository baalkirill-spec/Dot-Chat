package com.streamgram.feature.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.PrivacyAudience
import com.streamgram.core.model.PrivacySettings
import com.streamgram.domain.usecase.ObservePrivacySettingsUseCase
import com.streamgram.domain.usecase.UpdatePrivacySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    observePrivacySettingsUseCase: ObservePrivacySettingsUseCase,
    private val updatePrivacySettingsUseCase: UpdatePrivacySettingsUseCase,
) : ViewModel() {
    val uiState = observePrivacySettingsUseCase()
        .map { PrivacyUiState(settings = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PrivacyUiState(),
        )

    fun cyclePhoneVisibility() = update { it.copy(phoneNumberVisibility = it.phoneNumberVisibility.next()) }
    fun cycleUsernameVisibility() = update { it.copy(usernameVisibility = it.usernameVisibility.next()) }
    fun cycleAvatarVisibility() = update { it.copy(avatarVisibility = it.avatarVisibility.next()) }
    fun cycleLastSeenVisibility() = update { it.copy(lastSeenVisibility = it.lastSeenVisibility.next()) }
    fun cycleBioVisibility() = update { it.copy(bioVisibility = it.bioVisibility.next()) }
    fun cycleWhoCanCall() = update { it.copy(whoCanCall = it.whoCanCall.next()) }
    fun cycleWhoCanMessage() = update { it.copy(whoCanMessage = it.whoCanMessage.next()) }
    fun cycleWhoCanAddToChats() = update { it.copy(whoCanAddToChats = it.whoCanAddToChats.next()) }

    private fun update(transform: (PrivacySettings) -> PrivacySettings) {
        val snapshot = uiState.value.settings ?: return
        viewModelScope.launch {
            updatePrivacySettingsUseCase(transform(snapshot))
        }
    }
}

private fun PrivacyAudience.next(): PrivacyAudience {
    return when (this) {
        PrivacyAudience.EVERYONE -> PrivacyAudience.CONTACTS
        PrivacyAudience.CONTACTS -> PrivacyAudience.NOBODY
        PrivacyAudience.NOBODY -> PrivacyAudience.EVERYONE
    }
}
