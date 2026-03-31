package com.streamgram.core.model

import java.time.Instant

data class MediaPost(
    val id: String,
    val channelId: String,
    val kind: MediaPostKind,
    val attachments: List<MediaAttachment>,
    val caption: String,
    val hashtags: List<String>,
    val publishedAt: Instant,
    val stats: PostStats,
    val viewerState: PostViewerState,
)

data class MediaAttachment(
    val id: String,
    val kind: MediaAttachmentKind,
    val url: String,
    val previewUrl: String,
    val aspectRatio: Float,
    val durationSeconds: Int? = null,
)

data class PostStats(
    val commentsCount: Int,
    val sharesCount: Int,
    val savesCount: Int,
    val reactionSummary: ReactionSummary,
)

data class PostViewerState(
    val watchedSeconds: Int = 0,
    val watchCompletion: Float = 0f,
    val isSaved: Boolean = false,
    val hasImpression: Boolean = false,
)

enum class MediaPostKind {
    SHORT_VIDEO,
    LONG_VIDEO,
    PHOTO,
    CARD,
    ALBUM,
}

enum class MediaAttachmentKind {
    VIDEO,
    PHOTO,
}

data class FeedItem(
    val id: String,
    val post: MediaPost,
    val channel: Channel,
    val origin: FeedOrigin,
    val ranking: RecommendationScoreBreakdown,
)

enum class FeedOrigin {
    SUBSCRIBED_CHANNEL,
    RECOMMENDED_CHANNEL,
    TRENDING_FALLBACK,
}
