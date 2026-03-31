package com.streamgram.core.model

import java.time.Instant

data class RecommendationProfile(
    val userId: String,
    val interests: Map<String, Double>,
    val negativeSignals: Map<String, Double>,
    val followedChannelIds: Set<String>,
    val experimentBucket: String,
    val lastUpdatedAt: Instant,
)

data class RecommendationScoreBreakdown(
    val totalScore: Double,
    val signals: List<ScoreSignal>,
)

data class ScoreSignal(
    val name: String,
    val weight: Double,
    val contribution: Double,
    val explanation: String,
)

data class WatchEvent(
    val id: String,
    val userId: String,
    val postId: String,
    val channelId: String,
    val type: WatchEventType,
    val watchCompletion: Float,
    val watchedSeconds: Int,
    val occurredAt: Instant,
)

enum class WatchEventType {
    IMPRESSION,
    VIEW_START,
    WATCHED_2S,
    WATCHED_50_PERCENT,
    WATCHED_95_PERCENT,
    COMPLETED,
    SKIPPED,
    LIKED,
    DISLIKED,
    SHARED,
    COMMENTED,
    FOLLOWED_CHANNEL,
    SAVED,
    SENT_TO_FRIEND,
}
