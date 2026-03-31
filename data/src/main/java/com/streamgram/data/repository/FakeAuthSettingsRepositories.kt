package com.streamgram.data.repository

import com.streamgram.core.common.AppDispatchers
import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.model.AppSettings
import com.streamgram.core.model.AppSession
import com.streamgram.core.model.AuthProvider
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.ProfileSetupDraft
import com.streamgram.data.fake.FakeStreamGramStore
import com.streamgram.domain.repository.AuthRepository
import com.streamgram.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val EMAIL_OTP_LENGTH = 8

@Singleton
class FakeAuthRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val store: FakeStreamGramStore,
    dispatchers: AppDispatchers,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private var pendingEmailAddress: String = ""

    private val state: StateFlow<AuthState> = preferences.preferences
        .map { snapshot ->
            if (snapshot.hasAuthorizedSession) {
                AuthState.Authenticated(
                    AppSession(
                        user = store.currentUser,
                        phoneNumber = "+1 555 010 200",
                        authProvider = AuthProvider.SUPABASE_EMAIL_OTP,
                        accessToken = "demo-access-token",
                        refreshToken = "demo-refresh-token",
                        expiresAtEpochSeconds = 1_893_456_000,
                        deviceId = "android-local-demo",
                    ),
                )
            } else {
                AuthState.WaitPhone(suggestedCountryCode = "+1")
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Initial,
        )

    private val transientState = MutableStateFlow<AuthState?>(null)

    override fun observeAuthorizationState(): Flow<AuthState> {
        return combine(transientState, state) { transient, persisted ->
            transient ?: persisted
        }.stateIn(scope, SharingStarted.Eagerly, state.value)
    }

    override suspend fun markWelcomeSeen() {
        preferences.setHasSeenOnboarding(true)
    }

    override suspend fun submitPhoneNumber(phoneNumber: String, emailAddress: String) {
        if (phoneNumber.filter(Char::isDigit).length < 10) {
            transientState.value = AuthState.Error(
                message = "invalid_phone",
                recoverable = true,
            )
            return
        }
        if (!emailAddress.trim().lowercase(Locale.ROOT).matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))) {
            transientState.value = AuthState.Error(
                message = "invalid_email",
                recoverable = true,
            )
            return
        }
        transientState.value = AuthState.Loading
        pendingEmailAddress = emailAddress.trim().lowercase(Locale.ROOT)
        delay(850)
        transientState.value = AuthState.WaitCode(
            phoneNumber = phoneNumber,
            emailAddress = pendingEmailAddress,
            timeoutSeconds = 60,
            canResend = false,
        )
    }

    override suspend fun submitCode(code: String) {
        if (code.length != EMAIL_OTP_LENGTH) {
            transientState.value = AuthState.Error(
                message = "invalid_code",
                recoverable = true,
            )
            return
        }
        transientState.value = AuthState.Loading
        delay(700)
        if (code == "00000") {
            transientState.value = AuthState.WaitPassword(hint = "Account password")
            return
        }
        if (!preferences.preferences.first().hasCompletedProfileSetup) {
            transientState.value = AuthState.WaitProfileSetup(
                phoneNumber = currentPendingPhoneNumber(),
            )
            return
        }
        completeDemoAuthorization(phoneNumber = currentPendingPhoneNumber())
    }

    override suspend fun resendCode() {
        val phoneNumber = currentPendingPhoneNumber()
        transientState.value = AuthState.Loading
        delay(450)
        transientState.value = AuthState.WaitCode(
            phoneNumber = phoneNumber,
            emailAddress = pendingEmailAddress.ifBlank { "demo@dotchat.app" },
            timeoutSeconds = 60,
            canResend = false,
        )
    }

    override suspend fun submitPassword(password: String) {
        if (password.length < 4) {
            transientState.value = AuthState.Error(
                message = "invalid_password",
                recoverable = true,
            )
            return
        }
        transientState.value = AuthState.Loading
        delay(650)
        if (!preferences.preferences.first().hasCompletedProfileSetup) {
            transientState.value = AuthState.WaitProfileSetup(
                phoneNumber = currentPendingPhoneNumber(),
            )
            return
        }
        completeDemoAuthorization(phoneNumber = currentPendingPhoneNumber())
    }

    override suspend fun submitProfileSetup(draft: ProfileSetupDraft) {
        if (draft.firstName.isBlank() || draft.username.isBlank()) {
            transientState.value = AuthState.Error(
                message = "invalid_profile",
                recoverable = true,
            )
            return
        }
        transientState.value = AuthState.Loading
        delay(500)
        preferences.setHasCompletedProfileSetup(true)
        completeDemoAuthorization(phoneNumber = currentPendingPhoneNumber())
    }

    override suspend fun requestAccountDeletion() {
        // FakeAuthRepository: simulate account deletion via logout
        logout()
    }

    override suspend fun logout() {
        transientState.value = AuthState.Loading
        delay(300)
        preferences.setHasAuthorizedSession(false)
        transientState.value = AuthState.WaitPhone(suggestedCountryCode = "+1")
    }

    private suspend fun completeDemoAuthorization(phoneNumber: String) {
        preferences.setHasAuthorizedSession(true)
        transientState.value = AuthState.Authenticated(
            AppSession(
                user = store.currentUser,
                phoneNumber = phoneNumber,
                authProvider = AuthProvider.SUPABASE_EMAIL_OTP,
                accessToken = "demo-access-token",
                refreshToken = "demo-refresh-token",
                expiresAtEpochSeconds = 1_893_456_000,
                deviceId = "android-local-demo",
            ),
        )
    }

    private fun currentPendingPhoneNumber(): String {
        return when (val value = transientState.value) {
            is AuthState.WaitCode -> value.phoneNumber
            is AuthState.WaitProfileSetup -> value.phoneNumber
            else -> "+1 555 010 200"
        }
    }
}

@Singleton
class FakeSettingsRepository @Inject constructor(
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
