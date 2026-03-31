package com.streamgram.domain.usecase

import com.streamgram.core.model.FeedItem
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.WatchEvent
import com.streamgram.domain.repository.FeedRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePersonalizedFeedUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    operator fun invoke(): Flow<List<FeedItem>> = repository.observeFeed()
}

class RecordWatchEventUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(event: WatchEvent) = repository.recordWatchEvent(event)
}

class TogglePostReactionUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(postId: String, kind: ReactionKind, emoji: String? = null) {
        repository.toggleReaction(postId = postId, kind = kind, emoji = emoji)
    }
}

class ToggleSavedPostUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(postId: String, isSaved: Boolean) {
        repository.setSaved(postId = postId, isSaved = isSaved)
    }
}
