package com.streamgram.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.i18n.applyAppLanguage
import com.streamgram.core.runtimeconfig.RuntimeConfigRepository
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import com.streamgram.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RootViewModel @Inject constructor(
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
    observeSettingsUseCase: ObserveSettingsUseCase,
    runtimeConfigRepository: RuntimeConfigRepository,
) : ViewModel() {
    private val splashVisible = MutableStateFlow(true)

    val uiState = combine(
        splashVisible,
        observeAuthorizationStateUseCase(),
        observeSettingsUseCase(),
        runtimeConfigRepository.observeConfig(),
    ) { showSplash, authorizationState, settings, runtimeConfig ->
        RootUiState(
            showSplash = showSplash,
            authorizationState = authorizationState,
            shouldShowOnboarding = !settings.hasSeenOnboarding || !settings.hasAcceptedLegal,
            reducedMotionEnabled = runtimeConfig.experiments["motion_profile"] == "reduced",
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RootUiState(),
    )

    init {
        viewModelScope.launch {
            delay(1_350)
            splashVisible.value = false
        }
        viewModelScope.launch {
            observeSettingsUseCase()
                .map { it.selectedLanguageTag }
                .collect { applyAppLanguage(it) }
        }
    }
}
