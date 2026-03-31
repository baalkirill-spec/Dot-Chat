package com.streamgram.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.net.toUri
import com.streamgram.core.common.AppDispatchers
import com.streamgram.core.common.IdGenerator
import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.model.AppSession
import com.streamgram.core.model.AuthProvider
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.ProfileSetupDraft
import com.streamgram.core.model.User
import com.streamgram.core.supabase.DotChatSupabaseClient
import com.streamgram.core.supabase.SupabaseProfileRow
import com.streamgram.core.supabase.toUser
import com.streamgram.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val EMAIL_OTP_LENGTH = 8
private const val DEBUG_TEST_OTP = "12345678"

@Singleton
class SupabaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataSource,
    private val supabase: DotChatSupabaseClient,
    private val idGenerator: IdGenerator,
    dispatchers: AppDispatchers,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val transientState = MutableStateFlow<AuthState?>(null)
    private var pendingPhoneNumber: String = ""
    private var pendingEmailAddress: String = ""
    private var pendingVerifiedUserId: String? = null
    private var pendingSession: UserSession? = null

    private val persistedState: StateFlow<AuthState> = preferences.preferences
        .map { snapshot ->
            val sessionUserId = snapshot.sessionUserId
            val sessionAccessToken = snapshot.sessionAccessToken
            val sessionDisplayName = snapshot.sessionDisplayName
            if (
                snapshot.hasAuthorizedSession &&
                !sessionUserId.isNullOrBlank() &&
                !sessionAccessToken.isNullOrBlank() &&
                !sessionDisplayName.isNullOrBlank()
            ) {
                AuthState.Authenticated(
                    session = AppSession(
                        user = User(
                            id = sessionUserId,
                            handle = snapshot.sessionUsername ?: sessionUserId,
                            displayName = sessionDisplayName,
                            avatarUrl = snapshot.sessionAvatarUrl.orEmpty(),
                            bio = snapshot.sessionBio.orEmpty(),
                            subscriptionCount = 0,
                            chatCount = 0,
                            savedPostCount = 0,
                            username = snapshot.sessionUsername ?: sessionUserId,
                            phoneNumber = snapshot.sessionPhoneNumber,
                        ),
                        phoneNumber = snapshot.sessionPhoneNumber.orEmpty(),
                        authProvider = AuthProvider.SUPABASE_EMAIL_OTP,
                        accessToken = sessionAccessToken,
                        refreshToken = snapshot.sessionRefreshToken,
                        expiresAtEpochSeconds = snapshot.sessionExpiresAtEpochSeconds,
                        deviceId = snapshot.currentDeviceId ?: "android-device",
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

    init {
        scope.launch {
            restoreSupabaseSessionIfPossible()
        }
    }

    override fun observeAuthorizationState(): Flow<AuthState> {
        return combine(transientState, persistedState) { transient, persisted ->
            transient ?: persisted
        }.stateIn(scope, SharingStarted.Eagerly, persistedState.value)
    }

    override suspend fun markWelcomeSeen() {
        preferences.setHasSeenOnboarding(true)
    }

    override suspend fun submitPhoneNumber(phoneNumber: String, emailAddress: String) {
        if (!supabase.isConfigured) {
            transientState.value = AuthState.Error(message = "supabase_not_configured", recoverable = true)
            return
        }
        val normalized = normalizePhone(phoneNumber)
        val normalizedEmail = emailAddress.trim().lowercase(Locale.ROOT)
        if (normalized.filter(Char::isDigit).length < 10) {
            transientState.value = AuthState.Error(message = "invalid_phone", recoverable = true)
            return
        }
        if (!normalizedEmail.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))) {
            transientState.value = AuthState.Error(message = "invalid_email", recoverable = true)
            return
        }
        transientState.value = AuthState.Loading
        pendingPhoneNumber = normalized
        pendingEmailAddress = normalizedEmail
        runCatching {
            supabase.auth.signInWith(OTP) {
                email = normalizedEmail
                createUser = true
            }
        }.onSuccess {
            transientState.value = AuthState.WaitCode(
                phoneNumber = normalized,
                emailAddress = normalizedEmail,
                timeoutSeconds = 60,
                canResend = false,
            )
        }.onFailure { throwable ->
            if (isDebugBuild()) {
                transientState.value = AuthState.WaitCode(
                    phoneNumber = normalized,
                    emailAddress = normalizedEmail,
                    timeoutSeconds = 60,
                    canResend = true,
                )
            } else {
                transientState.value = AuthState.Error(
                    message = throwable.message ?: "auth_error_generic",
                    recoverable = true,
                )
            }
        }
    }

    override suspend fun submitCode(code: String) {
        if (isDebugBuild() && code == DEBUG_TEST_OTP) {
            finalizeDebugAuthenticatedSession()
            return
        }
        if (!supabase.isConfigured) {
            transientState.value = AuthState.Error(message = "supabase_not_configured", recoverable = true)
            return
        }
        if (code.length != EMAIL_OTP_LENGTH) {
            transientState.value = AuthState.Error(message = "invalid_code", recoverable = true)
            return
        }
        transientState.value = AuthState.Loading
        runCatching {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = pendingEmailAddress,
                token = code,
            )
            val session = requireNotNull(supabase.auth.currentSessionOrNull()) { "session_missing" }
            val userInfo = supabase.auth.currentUserOrNull() ?: supabase.auth.retrieveUserForCurrentSession()
            val profile = loadProfile(userInfo.id)
            pendingSession = session
            pendingVerifiedUserId = userInfo.id
            profile to session
        }.onSuccess { (profile, session) ->
            if (profile == null) {
                transientState.value = AuthState.WaitProfileSetup(
                    phoneNumber = pendingPhoneNumber,
                )
            } else {
                finalizeAuthenticatedSession(
                    user = profile.toUser(),
                    session = session,
                    phoneNumber = pendingPhoneNumber,
                )
            }
        }.onFailure { throwable ->
            transientState.value = AuthState.Error(
                message = throwable.message ?: "auth_error_generic",
                recoverable = true,
            )
        }
    }

    override suspend fun resendCode() {
        if (pendingPhoneNumber.isBlank()) {
            transientState.value = AuthState.WaitPhone(suggestedCountryCode = "+1")
            return
        }
        submitPhoneNumber(
            phoneNumber = pendingPhoneNumber,
            emailAddress = pendingEmailAddress,
        )
    }

    override suspend fun submitPassword(password: String) {
        transientState.value = AuthState.Error(
            message = "password_not_required",
            recoverable = true,
        )
    }

    override suspend fun submitProfileSetup(draft: ProfileSetupDraft) {
        if (!supabase.isConfigured) {
            transientState.value = AuthState.Error(message = "supabase_not_configured", recoverable = true)
            return
        }
        if (draft.firstName.isBlank() || draft.username.length !in 4..32) {
            transientState.value = AuthState.Error(message = "invalid_profile", recoverable = true)
            return
        }
        val pendingUserId = pendingVerifiedUserId
        val session = pendingSession
        if (pendingUserId == null || session == null) {
            transientState.value = AuthState.WaitPhone(suggestedCountryCode = "+1")
            return
        }
        transientState.value = AuthState.Loading
        runCatching {
            ensureUsernameAvailable(draft.username.trim(), pendingUserId)
            val avatarUrl = uploadAvatarIfPresent(userId = pendingUserId, avatarUri = draft.avatarUri)
            val profile = SupabaseProfileRow(
                id = pendingUserId,
                firstName = draft.firstName.trim(),
                lastName = draft.lastName.trim(),
                username = draft.username.trim().lowercase(),
                displayName = listOf(draft.firstName.trim(), draft.lastName.trim())
                    .filter(String::isNotBlank)
                    .joinToString(" ")
                    .ifBlank { draft.username.trim() },
                avatarUrl = avatarUrl,
                bio = draft.bio.trim(),
                phoneNumber = pendingPhoneNumber,
            )
            from(supabase.client, "profiles").upsert(profile)
            requireNotNull(loadProfile(pendingUserId)) { "profile_missing_after_upsert" }
        }.onSuccess { profile ->
            preferences.setHasCompletedProfileSetup(true)
            finalizeAuthenticatedSession(
                user = profile.toUser(),
                session = session,
                phoneNumber = pendingPhoneNumber,
            )
            pendingVerifiedUserId = null
            pendingSession = null
        }.onFailure { throwable ->
            transientState.value = AuthState.Error(
                message = throwable.message ?: "auth_error_generic",
                recoverable = true,
            )
        }
    }

    override suspend fun logout() {
        runCatching { supabase.auth.signOut() }
        preferences.clearSession()
        pendingPhoneNumber = ""
        pendingSession = null
        pendingVerifiedUserId = null
        pendingEmailAddress = ""
        transientState.value = AuthState.WaitPhone(suggestedCountryCode = "+1")
    }

    override suspend fun requestAccountDeletion() {
        if (!supabase.isConfigured) {
            error("supabase_not_configured")
        }
        val snapshot = preferences.preferences.first()
        val userId = snapshot.sessionUserId ?: error("not_authenticated")

        // Soft-delete: set account_status to pending_deletion
        from(supabase.client, "profiles").update(
            mapOf(
                "account_status" to "pending_deletion",
                "deleted_at" to java.time.Instant.now().toString(),
            ),
        ) {
            filter { eq("id", userId) }
        }

        // Terminate all device sessions for this user
        runCatching {
            from(supabase.client, "device_sessions").delete {
                filter { eq("user_id", userId) }
            }
        }

        // Sign out from Supabase and clear local state
        logout()
    }

    private suspend fun finalizeDebugAuthenticatedSession() {
        val handle = pendingEmailAddress.substringBefore("@")
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]"), "_")
            .ifBlank { "dot_user" }
        val displayName = pendingEmailAddress.substringBefore("@")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            .ifBlank { "Dot User" }
        val snapshot = preferences.preferences.first()
        val deviceId = snapshot.currentDeviceId ?: idGenerator.newId(prefix = "android")
        val debugUser = User(
            id = "debug-$handle",
            handle = handle,
            displayName = displayName,
            avatarUrl = "",
            bio = "",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
            username = handle,
            phoneNumber = pendingPhoneNumber,
        )
        preferences.setHasCompletedProfileSetup(true)
        preferences.storeSession(
            userId = debugUser.id,
            displayName = debugUser.displayName,
            username = debugUser.username,
            avatarUrl = null,
            bio = debugUser.bio,
            phoneNumber = pendingPhoneNumber,
            accessToken = "debug-local-token",
            refreshToken = null,
            expiresAtEpochSeconds = (System.currentTimeMillis() / 1000L) + 86_400L,
            deviceId = deviceId,
        )
        transientState.value = AuthState.Authenticated(
            session = AppSession(
                user = debugUser,
                phoneNumber = pendingPhoneNumber,
                authProvider = AuthProvider.SUPABASE_EMAIL_OTP,
                accessToken = "debug-local-token",
                refreshToken = null,
                expiresAtEpochSeconds = (System.currentTimeMillis() / 1000L) + 86_400L,
                deviceId = deviceId,
            ),
        )
    }

    private suspend fun restoreSupabaseSessionIfPossible() {
        if (!supabase.isConfigured) return
        val snapshot = preferences.preferences.first()
        val accessToken = snapshot.sessionAccessToken ?: return
        val refreshToken = snapshot.sessionRefreshToken ?: return
        runCatching {
            supabase.auth.importAuthToken(accessToken, refreshToken)
            val userInfo = supabase.auth.currentUserOrNull() ?: supabase.auth.retrieveUserForCurrentSession()
            val profile = loadProfile(userInfo.id) ?: return@runCatching
            val session = supabase.auth.currentSessionOrNull() ?: return@runCatching
            finalizeAuthenticatedSession(
                user = profile.toUser(),
                session = session,
                phoneNumber = profile.phoneNumber ?: snapshot.sessionPhoneNumber.orEmpty(),
            )
        }.onFailure {
            preferences.clearSession()
        }
    }

    private suspend fun loadProfile(userId: String): SupabaseProfileRow? {
        return from(supabase.client, "profiles")
            .select {
                filter { eq("id", userId) }
                single()
            }
            .decodeSingleOrNull<SupabaseProfileRow>()
    }

    private suspend fun ensureUsernameAvailable(username: String, currentUserId: String) {
        val existing = from(supabase.client, "profiles")
            .select {
                filter { eq("username", username.lowercase()) }
                limit(1)
            }
            .decodeList<SupabaseProfileRow>()
            .firstOrNull()
        if (existing != null && existing.id != currentUserId) {
            error("invalid_profile_username")
        }
    }

    private suspend fun uploadAvatarIfPresent(userId: String, avatarUri: String?): String? {
        if (avatarUri.isNullOrBlank()) return null
        val bytes = context.contentResolver.openInputStream(avatarUri.toUri())?.use { it.readBytes() } ?: return null
        val path = "avatars/$userId/${System.currentTimeMillis()}.jpg"
        supabase.storage
            .from(supabase.config.storageBucket)
            .upload(path = path, data = bytes)
        return supabase.storage.from(supabase.config.storageBucket).publicUrl(path)
    }

    private suspend fun finalizeAuthenticatedSession(
        user: User,
        session: UserSession,
        phoneNumber: String,
    ) {
        val snapshot = preferences.preferences.first()
        val deviceId = snapshot.currentDeviceId ?: idGenerator.newId(prefix = "android")
        preferences.storeSession(
            userId = user.id,
            displayName = user.displayName,
            username = user.username,
            avatarUrl = user.avatarUrl.takeIf { it.isNotBlank() },
            bio = user.bio,
            phoneNumber = phoneNumber,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAtEpochSeconds = runCatching { session.expiresAt.epochSeconds }.getOrNull(),
            deviceId = deviceId,
        )
        transientState.value = AuthState.Authenticated(
            session = AppSession(
                user = user,
                phoneNumber = phoneNumber,
                authProvider = AuthProvider.SUPABASE_EMAIL_OTP,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                expiresAtEpochSeconds = runCatching { session.expiresAt.epochSeconds }.getOrNull(),
                deviceId = deviceId,
            ),
        )
    }

    private fun normalizePhone(value: String): String {
        val raw = value.trim()
        if (raw.startsWith("+")) return raw
        return "+$raw"
    }

    private fun isDebugBuild(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}

private fun from(client: SupabaseClient, table: String) = client.from(table)
