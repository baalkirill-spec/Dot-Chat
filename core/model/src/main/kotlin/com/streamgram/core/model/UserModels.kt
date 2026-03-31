package com.streamgram.core.model

import java.time.Instant

data class User(
    val id: String,
    val handle: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val subscriptionCount: Int,
    val chatCount: Int,
    val savedPostCount: Int,
    val username: String = handle,
    val phoneNumber: String? = null,
)

data class Friend(
    val id: String,
    val displayName: String,
    val avatarUrl: String,
    val lastSeenAt: Instant,
)

data class FollowEvent(
    val id: String,
    val userId: String,
    val channelId: String,
    val followed: Boolean,
    val occurredAt: Instant,
)

data class SavedPost(
    val postId: String,
    val userId: String,
    val savedAt: Instant,
)

data class ActivityNotification(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val kind: ActivityNotificationKind,
    val actorAvatarUrl: String?,
)

enum class ActivityNotificationKind {
    FOLLOW,
    COMMENT,
    REACTION,
    FORWARD,
    SYSTEM,
}
