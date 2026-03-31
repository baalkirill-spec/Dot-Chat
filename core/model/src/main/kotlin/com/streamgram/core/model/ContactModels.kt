package com.streamgram.core.model

import java.time.Instant

data class Contact(
    val ownerUserId: String,
    val contactUserId: String,
    val user: User,
    val localAlias: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ContactSearchQuery(
    val rawQuery: String,
) {
    val normalized: String = rawQuery.trim()
    val isPhoneQuery: Boolean = normalized.any(Char::isDigit)
}

data class ContactSearchResult(
    val user: User,
    val matchedByUsername: Boolean,
    val matchedByPhone: Boolean,
    val alreadyInContacts: Boolean,
)

enum class ContactAction {
    MESSAGE,
    AUDIO_CALL,
    VIDEO_CALL,
    ADD,
    REMOVE,
    BLOCK,
    REPORT,
}
