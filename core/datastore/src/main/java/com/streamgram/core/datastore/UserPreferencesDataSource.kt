package com.streamgram.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferenceSnapshot(
    val mediaAutoplayEnabled: Boolean,
    val lastExperimentBucket: String,
    val selectedLanguageTag: String,
    val hasSeenOnboarding: Boolean,
    val hasAcceptedLegal: Boolean,
    val hasAuthorizedSession: Boolean,
    val hasCompletedProfileSetup: Boolean,
    val channelRecommendationsEnabled: Boolean,
    val richAnimationsEnabled: Boolean,
    val powerSavingModeEnabled: Boolean,
    val automaticPowerSavingEnabled: Boolean,
    val sessionUserId: String?,
    val sessionDisplayName: String?,
    val sessionUsername: String?,
    val sessionAvatarUrl: String?,
    val sessionBio: String?,
    val sessionPhoneNumber: String?,
    val sessionAccessToken: String?,
    val sessionRefreshToken: String?,
    val sessionExpiresAtEpochSeconds: Long?,
    val currentDeviceId: String?,
)

class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferenceSnapshot> = dataStore.data.map { prefs ->
        UserPreferenceSnapshot(
            mediaAutoplayEnabled = prefs[MediaAutoplayEnabledKey] ?: true,
            lastExperimentBucket = prefs[ExperimentBucketKey] ?: "mvp_control",
            selectedLanguageTag = prefs[SelectedLanguageKey] ?: "system",
            hasSeenOnboarding = prefs[HasSeenOnboardingKey] ?: false,
            hasAcceptedLegal = prefs[HasAcceptedLegalKey] ?: false,
            hasAuthorizedSession = prefs[HasAuthorizedSessionKey] ?: false,
            hasCompletedProfileSetup = prefs[HasCompletedProfileSetupKey] ?: false,
            channelRecommendationsEnabled = prefs[ChannelRecommendationsEnabledKey] ?: true,
            richAnimationsEnabled = prefs[RichAnimationsEnabledKey] ?: true,
            powerSavingModeEnabled = prefs[PowerSavingModeEnabledKey] ?: false,
            automaticPowerSavingEnabled = prefs[AutomaticPowerSavingEnabledKey] ?: true,
            sessionUserId = prefs[SessionUserIdKey],
            sessionDisplayName = prefs[SessionDisplayNameKey],
            sessionUsername = prefs[SessionUsernameKey],
            sessionAvatarUrl = prefs[SessionAvatarUrlKey],
            sessionBio = prefs[SessionBioKey],
            sessionPhoneNumber = prefs[SessionPhoneNumberKey],
            sessionAccessToken = prefs[SessionAccessTokenKey],
            sessionRefreshToken = prefs[SessionRefreshTokenKey],
            sessionExpiresAtEpochSeconds = prefs[SessionExpiresAtKey],
            currentDeviceId = prefs[CurrentDeviceIdKey],
        )
    }

    suspend fun setMediaAutoplayEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[MediaAutoplayEnabledKey] = enabled
        }
    }

    suspend fun setExperimentBucket(bucket: String) {
        dataStore.edit { prefs ->
            prefs[ExperimentBucketKey] = bucket
        }
    }

    suspend fun setSelectedLanguageTag(languageTag: String) {
        dataStore.edit { prefs ->
            prefs[SelectedLanguageKey] = languageTag
        }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        dataStore.edit { prefs ->
            prefs[HasSeenOnboardingKey] = seen
        }
    }

    suspend fun setHasAcceptedLegal(accepted: Boolean) {
        dataStore.edit { prefs ->
            prefs[HasAcceptedLegalKey] = accepted
        }
    }

    suspend fun setHasAuthorizedSession(hasSession: Boolean) {
        dataStore.edit { prefs ->
            prefs[HasAuthorizedSessionKey] = hasSession
        }
    }

    suspend fun setHasCompletedProfileSetup(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[HasCompletedProfileSetupKey] = completed
        }
    }

    suspend fun setChannelRecommendationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[ChannelRecommendationsEnabledKey] = enabled
        }
    }

    suspend fun setRichAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[RichAnimationsEnabledKey] = enabled
        }
    }

    suspend fun setPowerSavingModeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PowerSavingModeEnabledKey] = enabled
        }
    }

    suspend fun setAutomaticPowerSavingEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AutomaticPowerSavingEnabledKey] = enabled
        }
    }

    suspend fun storeSession(
        userId: String,
        displayName: String,
        username: String,
        avatarUrl: String?,
        bio: String,
        phoneNumber: String,
        accessToken: String,
        refreshToken: String?,
        expiresAtEpochSeconds: Long?,
        deviceId: String,
    ) {
        dataStore.edit { prefs ->
            prefs[HasAuthorizedSessionKey] = true
            prefs[SessionUserIdKey] = userId
            prefs[SessionDisplayNameKey] = displayName
            prefs[SessionUsernameKey] = username
            prefs[SessionAvatarUrlKey] = avatarUrl.orEmpty()
            prefs[SessionBioKey] = bio
            prefs[SessionPhoneNumberKey] = phoneNumber
            prefs[SessionAccessTokenKey] = accessToken
            refreshToken?.let { prefs[SessionRefreshTokenKey] = it } ?: prefs.remove(SessionRefreshTokenKey)
            expiresAtEpochSeconds?.let { prefs[SessionExpiresAtKey] = it } ?: prefs.remove(SessionExpiresAtKey)
            prefs[CurrentDeviceIdKey] = deviceId
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs[HasAuthorizedSessionKey] = false
            prefs.remove(SessionUserIdKey)
            prefs.remove(SessionDisplayNameKey)
            prefs.remove(SessionUsernameKey)
            prefs.remove(SessionAvatarUrlKey)
            prefs.remove(SessionBioKey)
            prefs.remove(SessionPhoneNumberKey)
            prefs.remove(SessionAccessTokenKey)
            prefs.remove(SessionRefreshTokenKey)
            prefs.remove(SessionExpiresAtKey)
            prefs.remove(CurrentDeviceIdKey)
        }
    }

    private companion object {
        val MediaAutoplayEnabledKey = booleanPreferencesKey("media_autoplay_enabled")
        val ExperimentBucketKey = stringPreferencesKey("experiment_bucket")
        val SelectedLanguageKey = stringPreferencesKey("selected_language_tag")
        val HasSeenOnboardingKey = booleanPreferencesKey("has_seen_onboarding")
        val HasAcceptedLegalKey = booleanPreferencesKey("has_accepted_legal")
        val HasAuthorizedSessionKey = booleanPreferencesKey("has_authorized_session")
        val HasCompletedProfileSetupKey = booleanPreferencesKey("has_completed_profile_setup")
        val ChannelRecommendationsEnabledKey = booleanPreferencesKey("channel_recommendations_enabled")
        val RichAnimationsEnabledKey = booleanPreferencesKey("rich_animations_enabled")
        val PowerSavingModeEnabledKey = booleanPreferencesKey("power_saving_mode_enabled")
        val AutomaticPowerSavingEnabledKey = booleanPreferencesKey("automatic_power_saving_enabled")
        val SessionUserIdKey = stringPreferencesKey("session_user_id")
        val SessionDisplayNameKey = stringPreferencesKey("session_display_name")
        val SessionUsernameKey = stringPreferencesKey("session_username")
        val SessionAvatarUrlKey = stringPreferencesKey("session_avatar_url")
        val SessionBioKey = stringPreferencesKey("session_bio")
        val SessionPhoneNumberKey = stringPreferencesKey("session_phone_number")
        val SessionAccessTokenKey = stringPreferencesKey("session_access_token")
        val SessionRefreshTokenKey = stringPreferencesKey("session_refresh_token")
        val SessionExpiresAtKey = longPreferencesKey("session_expires_at")
        val CurrentDeviceIdKey = stringPreferencesKey("session_current_device_id")
    }
}
