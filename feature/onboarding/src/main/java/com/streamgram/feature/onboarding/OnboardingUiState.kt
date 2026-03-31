package com.streamgram.feature.onboarding

import com.streamgram.core.i18n.AppLanguage

data class OnboardingUiState(
    val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
    val step: OnboardingStep = OnboardingStep.LANGUAGE,
    val legalAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
)

enum class OnboardingStep {
    LANGUAGE,
    WELCOME,
    LEGAL,
}
