package com.streamgram.core.model

enum class PrivacyAudience {
    EVERYONE,
    CONTACTS,
    NOBODY,
}

data class PrivacySettings(
    val phoneNumberVisibility: PrivacyAudience,
    val usernameVisibility: PrivacyAudience,
    val avatarVisibility: PrivacyAudience,
    val lastSeenVisibility: PrivacyAudience,
    val bioVisibility: PrivacyAudience,
    val whoCanAddToChats: PrivacyAudience,
    val whoCanCall: PrivacyAudience,
    val whoCanMessage: PrivacyAudience,
)

data class BlockedUser(
    val userId: String,
    val createdAtEpochSeconds: Long,
    val reason: String? = null,
)
