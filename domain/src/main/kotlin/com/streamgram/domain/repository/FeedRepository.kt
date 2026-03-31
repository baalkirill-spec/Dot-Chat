package com.streamgram.domain.repository

import com.streamgram.core.common.AppResult
import com.streamgram.core.model.FeedItem
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.WatchEvent
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun observeFeed(): Flow<List<FeedItem>>
    suspend fun refreshFeed(): AppResult<Unit>
    suspend fun recordWatchEvent(event: WatchEvent)
    suspend fun toggleReaction(postId: String, kind: ReactionKind, emoji: String? = null)
    suspend fun setSaved(postId: String, isSaved: Boolean)
}
