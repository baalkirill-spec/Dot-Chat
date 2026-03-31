package com.streamgram.core.model

sealed interface AuthState {
    data object Initial : AuthState
    data object Loading : AuthState
    data class WaitPhone(
        val suggestedCountryCode: String? = null,
    ) : AuthState

    data class WaitCode(
        val phoneNumber: String,
        val emailAddress: String,
        val timeoutSeconds: Int,
        val canResend: Boolean,
    ) : AuthState

    data class WaitPassword(
        val hint: String?,
    ) : AuthState

    data class WaitProfileSetup(
        val phoneNumber: String,
        val draft: ProfileSetupDraft = ProfileSetupDraft(),
    ) : AuthState

    data class Authenticated(
        val session: AppSession,
    ) : AuthState

    data class Error(
        val message: String,
        val recoverable: Boolean,
    ) : AuthState
}

data class AppSession(
    val user: User,
    val phoneNumber: String,
    val authProvider: AuthProvider,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long?,
    val deviceId: String,
)

enum class AuthProvider {
    PHONE_OTP,
    SUPABASE_PHONE,
    SUPABASE_EMAIL_OTP,
}

data class ProfileSetupDraft(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
)

data class PhoneVerificationSession(
    val phoneNumber: String,
    val resendAvailableInSeconds: Int,
    val attemptId: String,
)
