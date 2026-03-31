package com.streamgram.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.AuthState
import com.streamgram.domain.usecase.LogoutUseCase
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import com.streamgram.domain.usecase.ObserveSettingsUseCase
import com.streamgram.domain.usecase.SetAutomaticPowerSavingEnabledUseCase
import com.streamgram.domain.usecase.SetChannelRecommendationsEnabledUseCase
import com.streamgram.domain.usecase.SetMediaAutoplayEnabledUseCase
import com.streamgram.domain.usecase.SetPowerSavingModeEnabledUseCase
import com.streamgram.domain.usecase.SetRichAnimationsEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setMediaAutoplayEnabledUseCase: SetMediaAutoplayEnabledUseCase,
    private val setChannelRecommendationsEnabledUseCase: SetChannelRecommendationsEnabledUseCase,
    private val setRichAnimationsEnabledUseCase: SetRichAnimationsEnabledUseCase,
    private val setPowerSavingModeEnabledUseCase: SetPowerSavingModeEnabledUseCase,
    private val setAutomaticPowerSavingEnabledUseCase: SetAutomaticPowerSavingEnabledUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val uiState = kotlinx.coroutines.flow.combine(
        observeAuthorizationStateUseCase(),
        observeSettingsUseCase(),
    ) { authState, settings ->
        SettingsUiState(
            settings = settings,
            currentUser = (authState as? AuthState.Authenticated)?.session?.user,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onMediaAutoplayChanged(enabled: Boolean) {
        viewModelScope.launch { setMediaAutoplayEnabledUseCase(enabled) }
    }

    fun onChannelRecommendationsChanged(enabled: Boolean) {
        viewModelScope.launch { setChannelRecommendationsEnabledUseCase(enabled) }
    }

    fun onRichAnimationsChanged(enabled: Boolean) {
        viewModelScope.launch { setRichAnimationsEnabledUseCase(enabled) }
    }

    fun onPowerSavingChanged(enabled: Boolean) {
        viewModelScope.launch { setPowerSavingModeEnabledUseCase(enabled) }
    }

    fun onAutomaticPowerSavingChanged(enabled: Boolean) {
        viewModelScope.launch { setAutomaticPowerSavingEnabledUseCase(enabled) }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
