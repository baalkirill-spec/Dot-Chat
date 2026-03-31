package com.streamgram.feature.auth

import com.streamgram.core.model.AuthState
import com.streamgram.core.model.ProfileSetupDraft

data class AuthUiState(
    val authorizationState: AuthState = AuthState.Initial,
    val phoneInput: String = "",
    val emailInput: String = "",
    val verificationDestination: String = "",
    val awaitingVerification: Boolean = false,
    val codeInput: String = "",
    val passwordInput: String = "",
    val profileDraft: ProfileSetupDraft = ProfileSetupDraft(),
    val activeStep: AuthStep = AuthStep.EMAIL,
    val profileSetupStep: ProfileSetupStep = ProfileSetupStep.NAME,
    val resendRemainingSeconds: Int = 0,
    val canResend: Boolean = false,
    val errorKey: String? = null,
    val isSubmitting: Boolean = false,
)

enum class AuthStep {
    EMAIL,
    PHONE,
    CODE,
    PASSWORD,
    PROFILE_SETUP,
}

enum class ProfileSetupStep {
    NAME,
    USERNAME,
    PHOTO,
    BIO,
}
