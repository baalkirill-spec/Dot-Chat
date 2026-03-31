package com.streamgram.data.fake

import com.streamgram.core.common.TimeProvider
import com.streamgram.core.model.Channel
import com.streamgram.core.model.FeedItem
import com.streamgram.core.model.FeedOrigin
import com.streamgram.core.model.MediaPost
import com.streamgram.core.model.RecommendationProfile
import com.streamgram.core.model.RecommendationScoreBreakdown
import com.streamgram.core.model.ScoreSignal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class RecommendationScorer @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    fun rank(
        posts: List<MediaPost>,
        channelsById: Map<String, Channel>,
        profile: RecommendationProfile,
    ): List<FeedItem> {
        return posts.mapNotNull { post ->
            val channel = channelsById[post.channelId] ?: return@mapNotNull null
            val ranking = buildScore(post = post, channel = channel, profile = profile)
            FeedItem(
                id = post.id,
                post = post,
                channel = channel,
                origin = if (channel.isSubscribed) FeedOrigin.SUBSCRIBED_CHANNEL else FeedOrigin.RECOMMENDED_CHANNEL,
                ranking = ranking,
            )
        }.sortedByDescending { it.ranking.totalScore }
    }

    private fun buildScore(
        post: MediaPost,
        channel: Channel,
        profile: RecommendationProfile,
    ): RecommendationScoreBreakdown {
        val topicAffinity = channel.topics.map { profile.interests[it] ?: 0.0 }.averageOrZero()
        val negativeAffinity = channel.topics.map { profile.negativeSignals[it] ?: 0.0 }.averageOrZero()
        val freshnessHours = (timeProvider.now().epochSecond - post.publishedAt.epochSecond) / 3600.0
        val freshness = max(0.25, 1.6 - freshnessHours / 24.0)
        val completion = post.viewerState.watchCompletion.toDouble() * 2.2
        val followBoost = if (channel.id in profile.followedChannelIds || channel.isSubscribed) 2.4 else 0.4
        val skipPenalty = if (post.viewerState.hasImpression && post.viewerState.watchCompletion < 0.2f) -1.35 else 0.0
        val saveBoost = if (post.viewerState.isSaved) 0.75 else 0.0
        val popularity = min((post.stats.reactionSummary.likeCount + post.stats.sharesCount * 8) / 5_000.0, 0.95)
        val signalList = listOf(
            ScoreSignal("follow_boost", 1.0, followBoost, "Приоритет каналов, на которые уже подписан пользователь."),
            ScoreSignal("topic_affinity", 1.8, topicAffinity * 1.8, "Совпадение тем поста с профилем интересов."),
            ScoreSignal("negative_affinity", 1.2, -negativeAffinity * 1.2, "Снижение веса по темам, которые пользователь быстро свайпает."),
            ScoreSignal("watch_completion", 2.2, completion, "Досмотр похожего контента усиливает вероятность показа."),
            ScoreSignal("freshness", 1.0, freshness, "Свежесть контента даёт бонус в mixed feed."),
            ScoreSignal("popularity", 0.95, popularity, "Лёгкий quality prior по органическим реакциям и репостам."),
            ScoreSignal("save_boost", 0.75, saveBoost, "Сохранённые материалы усиливают похожие рекомендации."),
            ScoreSignal("quick_skip_penalty", 1.35, skipPenalty, "Быстрый skip снижает вес похожих роликов."),
        )
        return RecommendationScoreBreakdown(
            totalScore = signalList.sumOf { it.contribution },
            signals = signalList,
        )
    }

    private fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
}
