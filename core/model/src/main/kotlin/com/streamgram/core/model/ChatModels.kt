package com.streamgram.core.model

import java.time.Instant

data class Chat(
    val id: String,
    val title: String,
    val description: String = "",
    val avatarUrl: String?,
    val visibility: ConversationVisibility = ConversationVisibility.PRIVATE,
    val members: List<User>,
    val unreadCount: Int,
    val isMuted: Boolean,
    val lastMessagePreview: String,
    val lastActivityAt: Instant,
)

data class Message(
    val id: String,
    val chatId: String,
    val sender: User,
    val sentAt: Instant,
    val kind: MessageKind,
    val text: String,
    val attachments: List<MediaAttachment>,
    val forwardedPostId: String? = null,
    val isOutgoing: Boolean,
    val replyToMessageId: String? = null,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.SENT,
    val readStatus: MessageReadStatus = MessageReadStatus.READ,
)

enum class MessageKind {
    TEXT,
    MEDIA,
    FORWARDED_POST,
    SYSTEM,
}

enum class MessageDeliveryStatus {
    DRAFT,
    QUEUED,
    UPLOADING,
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
}

enum class MessageReadStatus {
    PENDING,
    READ,
}

data class TypingIndicator(
    val chatId: String,
    val userId: String,
    val expiresAtEpochMs: Long,
)

data class FriendForward(
    val id: String,
    val postId: String,
    val targetChatId: String,
    val targetUserId: String?,
    val sentAt: Instant,
)
