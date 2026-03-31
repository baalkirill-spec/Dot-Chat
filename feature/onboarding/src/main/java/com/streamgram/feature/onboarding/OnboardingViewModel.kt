package com.streamgram.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.i18n.AppLanguage
import com.streamgram.domain.usecase.ObserveSettingsUseCase
import com.streamgram.domain.usecase.SetAppLanguageUseCase
import com.streamgram.domain.usecase.SetLegalAcceptedUseCase
import com.streamgram.domain.usecase.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val setLegalAcceptedUseCase: SetLegalAcceptedUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
) : ViewModel() {
    private val step = MutableStateFlow(OnboardingStep.LANGUAGE)
    private val isSubmitting = MutableStateFlow(false)

    val uiState = combine(
        observeSettingsUseCase(),
        step,
        isSubmitting,
    ) { settings, stepValue, submitting ->
        OnboardingUiState(
            selectedLanguage = AppLanguage.fromTag(settings.selectedLanguageTag),
            step = stepValue,
            legalAccepted = settings.hasAcceptedLegal,
            isSubmitting = submitting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState(),
    )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            setAppLanguageUseCase(language.tag)
        }
    }

    fun continueFromLanguage() {
        step.value = OnboardingStep.WELCOME
    }

    fun continueFromWelcome() {
        step.value = OnboardingStep.LEGAL
    }

    fun setLegalAccepted(accepted: Boolean) {
        viewModelScope.launch {
            setLegalAcceptedUseCase(accepted)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            isSubmitting.value = true
            setLegalAcceptedUseCase(true)
            setOnboardingCompletedUseCase(true)
            isSubmitting.value = false
        }
    }

    fun back() {
        step.update {
            when (it) {
                OnboardingStep.LANGUAGE -> OnboardingStep.LANGUAGE
                OnboardingStep.WELCOME -> OnboardingStep.LANGUAGE
                OnboardingStep.LEGAL -> OnboardingStep.WELCOME
            }
        }
    }
}
