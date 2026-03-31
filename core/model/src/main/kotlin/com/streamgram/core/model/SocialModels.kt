package com.streamgram.core.model

import java.time.Instant

data class Reaction(
    val userId: String,
    val targetId: String,
    val targetType: ReactionTargetType,
    val kind: ReactionKind,
    val emoji: String? = null,
    val createdAt: Instant,
)

data class ReactionSummary(
    val likeCount: Int,
    val dislikeCount: Int,
    val emojiCounts: Map<String, Int>,
    val viewerReaction: ViewerReaction = ViewerReaction.None,
)

sealed interface ViewerReaction {
    data object None : ViewerReaction
    data object Like : ViewerReaction
    data object Dislike : ViewerReaction
    data class Emoji(val value: String) : ViewerReaction
}

enum class ReactionKind {
    LIKE,
    DISLIKE,
    EMOJI,
}

enum class ReactionTargetType {
    POST,
    COMMENT,
}

data class Comment(
    val id: String,
    val postId: String,
    val parentCommentId: String?,
    val author: User,
    val message: String,
    val createdAt: Instant,
    val replyCount: Int,
    val reactions: ReactionSummary,
    val moderationState: CommentModerationState,
)

enum class CommentModerationState {
    VISIBLE,
    PENDING_REVIEW,
    HIDDEN,
}

enum class CommentSort {
    TOP,
    NEW,
}

data class ShareEvent(
    val id: String,
    val postId: String,
    val actorId: String,
    val target: ShareTarget,
    val occurredAt: Instant,
)

enum class ShareTarget {
    PROFILE_REPOST,
    CHAT_FORWARD,
    ANDROID_SHARE,
}
