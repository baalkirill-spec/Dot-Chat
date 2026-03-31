package com.streamgram.core.supabase

import com.streamgram.core.model.ActivityNotification
import com.streamgram.core.model.ActivityNotificationKind
import com.streamgram.core.model.BlockedUser
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentModerationState
import com.streamgram.core.model.Contact
import com.streamgram.core.model.ConversationVisibility
import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.MediaAttachment
import com.streamgram.core.model.MediaAttachmentKind
import com.streamgram.core.model.Message
import com.streamgram.core.model.MessageDeliveryStatus
import com.streamgram.core.model.MessageKind
import com.streamgram.core.model.MessageReadStatus
import com.streamgram.core.model.PrivacyAudience
import com.streamgram.core.model.PrivacySettings
import com.streamgram.core.model.ReactionSummary
import com.streamgram.core.model.SecurityNotification
import com.streamgram.core.model.User
import com.streamgram.core.model.ViewerReaction
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfileRow(
    val id: String,
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val username: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String = "",
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("subscription_count") val subscriptionCount: Int = 0,
    @SerialName("chat_count") val chatCount: Int = 0,
    @SerialName("saved_post_count") val savedPostCount: Int = 0,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class SupabaseContactRow(
    val id: String,
    @SerialName("owner_user_id") val ownerUserId: String,
    @SerialName("contact_user_id") val contactUserId: String,
    @SerialName("local_alias") val localAlias: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SupabaseChatMembershipRow(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val visibility: String = "private",
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("is_muted") val isMuted: Boolean = false,
    @SerialName("last_message_preview") val lastMessagePreview: String = "",
    @SerialName("last_activity_at") val lastActivityAt: String,
)

@Serializable
data class SupabaseChatMemberRow(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String,
)

@Serializable
data class SupabaseMessageAttachmentRow(
    val id: String,
    val kind: String,
    val url: String,
    @SerialName("preview_url") val previewUrl: String = url,
    @SerialName("aspect_ratio") val aspectRatio: Float = 1f,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
)

@Serializable
data class SupabaseMessageRow(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("message_type") val messageType: String = "text",
    val text: String = "",
    val attachments: List<SupabaseMessageAttachmentRow> = emptyList(),
    @SerialName("forwarded_post_id") val forwardedPostId: String? = null,
    @SerialName("reply_to_message_id") val replyToMessageId: String? = null,
    @SerialName("is_outgoing") val isOutgoing: Boolean = false,
    @SerialName("delivery_status") val deliveryStatus: String = "sent",
    @SerialName("read_status") val readStatus: String = "pending",
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SupabaseChannelRow(
    val id: String,
    val title: String,
    val handle: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val description: String = "",
    val topics: List<String> = emptyList(),
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("is_subscribed") val isSubscribed: Boolean = false,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("cover_url") val coverUrl: String? = null,
    val visibility: String = "public",
    @SerialName("invite_link") val inviteLink: String? = null,
)

@Serializable
data class SupabaseCommentRow(
    val id: String,
    @SerialName("post_id") val postId: String,
    @SerialName("author_id") val authorId: String,
    val message: String,
    @SerialName("parent_comment_id") val parentCommentId: String? = null,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("dislike_count") val dislikeCount: Int = 0,
    @SerialName("viewer_reaction") val viewerReaction: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("moderation_state") val moderationState: String = "visible",
)

@Serializable
data class SupabaseActivityNotificationRow(
    val id: String,
    val title: String,
    val body: String,
    val kind: String = "system",
    @SerialName("actor_avatar_url") val actorAvatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SupabasePrivacySettingsRow(
    @SerialName("user_id") val userId: String,
    @SerialName("phone_number_visibility") val phoneNumberVisibility: String = "contacts",
    @SerialName("username_visibility") val usernameVisibility: String = "everyone",
    @SerialName("avatar_visibility") val avatarVisibility: String = "everyone",
    @SerialName("last_seen_visibility") val lastSeenVisibility: String = "contacts",
    @SerialName("bio_visibility") val bioVisibility: String = "everyone",
    @SerialName("who_can_add_to_chats") val whoCanAddToChats: String = "contacts",
    @SerialName("who_can_call") val whoCanCall: String = "contacts",
    @SerialName("who_can_message") val whoCanMessage: String = "everyone",
)

@Serializable
data class SupabaseBlockedUserRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    val reason: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class SupabaseDeviceSessionRow(
    val id: String,
    @SerialName("device_name") val deviceName: String,
    val platform: String,
    @SerialName("ip_country_hint") val ipCountryHint: String? = null,
    @SerialName("is_current") val isCurrent: Boolean = false,
    @SerialName("last_active_at") val lastActiveAt: String,
    @SerialName("signed_in_at") val signedInAt: String,
)

@Serializable
data class SupabaseSecurityNotificationRow(
    val id: String,
    val title: String,
    val body: String,
    @SerialName("created_at") val createdAt: String,
)

fun SupabaseProfileRow.toUser(): User {
    val resolvedDisplayName = displayName.ifBlank {
        listOf(firstName, lastName)
            .filter(String::isNotBlank)
            .joinToString(" ")
            .ifBlank { username.ifBlank { id } }
    }
    return User(
        id = id,
        handle = username.ifBlank { id },
        displayName = resolvedDisplayName,
        avatarUrl = avatarUrl.orEmpty(),
        bio = bio,
        subscriptionCount = subscriptionCount,
        chatCount = chatCount,
        savedPostCount = savedPostCount,
        username = username.ifBlank { id },
        phoneNumber = phoneNumber,
    )
}

fun SupabaseContactRow.toContact(user: User): Contact {
    return Contact(
        ownerUserId = ownerUserId,
        contactUserId = contactUserId,
        user = user,
        localAlias = localAlias,
        isFavorite = isFavorite,
        createdAt = createdAt.toInstantOrNow(),
        updatedAt = updatedAt.toInstantOrNow(),
    )
}

fun SupabaseChatMembershipRow.toChat(members: List<User>): Chat {
    return Chat(
        id = chatId,
        title = title,
        description = description,
        avatarUrl = avatarUrl,
        visibility = visibility.toConversationVisibility(),
        members = members,
        unreadCount = unreadCount,
        isMuted = isMuted,
        lastMessagePreview = lastMessagePreview,
        lastActivityAt = lastActivityAt.toInstantOrNow(),
    )
}

fun SupabaseMessageRow.toMessage(sender: User): Message {
    return Message(
        id = id,
        chatId = chatId,
        sender = sender,
        sentAt = createdAt.toInstantOrNow(),
        kind = messageType.toMessageKind(),
        text = text,
        attachments = attachments.map { it.toAttachment() },
        forwardedPostId = forwardedPostId,
        isOutgoing = isOutgoing,
        replyToMessageId = replyToMessageId,
        deliveryStatus = deliveryStatus.toDeliveryStatus(),
        readStatus = readStatus.toReadStatus(),
    )
}

fun SupabaseChannelRow.toChannel(): Channel {
    return Channel(
        id = id,
        title = title,
        handle = handle.ifBlank { "@$title" },
        avatarUrl = avatarUrl.orEmpty(),
        description = description,
        topics = topics.toSet(),
        followerCount = followerCount,
        isSubscribed = isSubscribed,
        isVerified = isVerified,
        coverUrl = coverUrl,
        visibility = visibility.toConversationVisibility(),
        inviteLink = inviteLink,
    )
}

fun SupabaseCommentRow.toComment(author: User): Comment {
    return Comment(
        id = id,
        postId = postId,
        author = author,
        message = message,
        createdAt = createdAt.toInstantOrNow(),
        replyCount = replyCount,
        reactions = ReactionSummary(
            likeCount = likeCount,
            dislikeCount = dislikeCount,
            emojiCounts = viewerReaction?.let { mapOf(it to 1) }.orEmpty(),
            viewerReaction = viewerReaction.toViewerReaction(),
        ),
        moderationState = moderationState.toModerationState(),
        parentCommentId = parentCommentId,
    )
}

fun SupabaseActivityNotificationRow.toNotification(): ActivityNotification {
    return ActivityNotification(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt.toInstantOrNow(),
        kind = kind.toActivityKind(),
        actorAvatarUrl = actorAvatarUrl,
    )
}

fun SupabasePrivacySettingsRow.toPrivacySettings(): PrivacySettings {
    return PrivacySettings(
        phoneNumberVisibility = phoneNumberVisibility.toAudience(),
        usernameVisibility = usernameVisibility.toAudience(),
        avatarVisibility = avatarVisibility.toAudience(),
        lastSeenVisibility = lastSeenVisibility.toAudience(),
        bioVisibility = bioVisibility.toAudience(),
        whoCanAddToChats = whoCanAddToChats.toAudience(),
        whoCanCall = whoCanCall.toAudience(),
        whoCanMessage = whoCanMessage.toAudience(),
    )
}

fun SupabaseBlockedUserRow.toBlockedUser(): BlockedUser {
    return BlockedUser(
        userId = userId,
        createdAtEpochSeconds = createdAt.toInstantOrNow().epochSecond,
        reason = reason,
    )
}

fun SupabaseDeviceSessionRow.toDeviceSession(): DeviceSession {
    return DeviceSession(
        id = id,
        deviceName = deviceName,
        platform = platform,
        ipCountryHint = ipCountryHint,
        isCurrent = isCurrent,
        lastActiveAtEpochSeconds = lastActiveAt.toInstantOrNow().epochSecond,
        signedInAtEpochSeconds = signedInAt.toInstantOrNow().epochSecond,
    )
}

fun SupabaseSecurityNotificationRow.toSecurityNotification(): SecurityNotification {
    return SecurityNotification(
        id = id,
        title = title,
        body = body,
        createdAtEpochSeconds = createdAt.toInstantOrNow().epochSecond,
    )
}

private fun SupabaseMessageAttachmentRow.toAttachment(): MediaAttachment {
    return MediaAttachment(
        id = id,
        kind = kind.toAttachmentKind(),
        url = url,
        previewUrl = previewUrl,
        aspectRatio = aspectRatio,
        durationSeconds = durationSeconds,
    )
}

private fun String.toInstantOrNow(): Instant = runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

private fun String.toConversationVisibility(): ConversationVisibility {
    return if (equals("public", ignoreCase = true)) ConversationVisibility.PUBLIC else ConversationVisibility.PRIVATE
}

private fun String.toMessageKind(): MessageKind {
    return when (lowercase()) {
        "media" -> MessageKind.MEDIA
        "forwarded_post" -> MessageKind.FORWARDED_POST
        "system" -> MessageKind.SYSTEM
        else -> MessageKind.TEXT
    }
}

private fun String.toDeliveryStatus(): MessageDeliveryStatus {
    return when (lowercase()) {
        "draft" -> MessageDeliveryStatus.DRAFT
        "queued" -> MessageDeliveryStatus.QUEUED
        "uploading" -> MessageDeliveryStatus.UPLOADING
        "sending" -> MessageDeliveryStatus.SENDING
        "delivered" -> MessageDeliveryStatus.DELIVERED
        "read" -> MessageDeliveryStatus.READ
        "failed" -> MessageDeliveryStatus.FAILED
        else -> MessageDeliveryStatus.SENT
    }
}

private fun String.toReadStatus(): MessageReadStatus {
    return if (equals("read", ignoreCase = true)) MessageReadStatus.READ else MessageReadStatus.PENDING
}

private fun String.toAttachmentKind(): MediaAttachmentKind {
    return if (equals("video", ignoreCase = true)) MediaAttachmentKind.VIDEO else MediaAttachmentKind.PHOTO
}

private fun String?.toViewerReaction(): ViewerReaction {
    if (this.isNullOrBlank()) return ViewerReaction.None
    return when (lowercase()) {
        "like" -> ViewerReaction.Like
        "dislike" -> ViewerReaction.Dislike
        else -> ViewerReaction.Emoji(this)
    }
}

private fun String.toModerationState(): CommentModerationState {
    return when (lowercase()) {
        "hidden" -> CommentModerationState.HIDDEN
        "pending_review" -> CommentModerationState.PENDING_REVIEW
        else -> CommentModerationState.VISIBLE
    }
}

private fun String.toActivityKind(): ActivityNotificationKind {
    return when (lowercase()) {
        "follow" -> ActivityNotificationKind.FOLLOW
        "comment" -> ActivityNotificationKind.COMMENT
        "reaction" -> ActivityNotificationKind.REACTION
        "forward" -> ActivityNotificationKind.FORWARD
        else -> ActivityNotificationKind.SYSTEM
    }
}

private fun String.toAudience(): PrivacyAudience {
    return when (lowercase()) {
        "everyone" -> PrivacyAudience.EVERYONE
        "nobody" -> PrivacyAudience.NOBODY
        else -> PrivacyAudience.CONTACTS
    }
}
