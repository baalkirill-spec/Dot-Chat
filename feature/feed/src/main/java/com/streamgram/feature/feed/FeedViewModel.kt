package com.streamgram.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.common.IdGenerator
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.featureflags.FeatureFlagKey
import com.streamgram.core.featureflags.FeatureFlagRepository
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.WatchEvent
import com.streamgram.core.model.WatchEventType
import com.streamgram.domain.repository.FeedRepository
import com.streamgram.domain.usecase.ObservePersonalizedFeedUseCase
import com.streamgram.domain.usecase.ToggleFollowChannelUseCase
import com.streamgram.domain.usecase.TogglePostReactionUseCase
import com.streamgram.domain.usecase.ToggleSavedPostUseCase
import com.streamgram.domain.usecase.RecordWatchEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FeedViewModel @Inject constructor(
    observePersonalizedFeedUseCase: ObservePersonalizedFeedUseCase,
    featureFlagRepository: FeatureFlagRepository,
    private val feedRepository: FeedRepository,
    private val recordWatchEventUseCase: RecordWatchEventUseCase,
    private val togglePostReactionUseCase: TogglePostReactionUseCase,
    private val toggleSavedPostUseCase: ToggleSavedPostUseCase,
    private val toggleFollowChannelUseCase: ToggleFollowChannelUseCase,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    val uiState = combine(
        observePersonalizedFeedUseCase(),
        featureFlagRepository.observeFlags(),
    ) { items, flags ->
        FeedUiState(
            items = items,
            autoplayEnabled = flags[FeatureFlagKey.AUTOPLAY]?.enabled ?: true,
            commentsEnabled = flags[FeatureFlagKey.COMMENTS]?.enabled ?: true,
            reactionsEnabled = flags[FeatureFlagKey.LIKE_DISLIKE]?.enabled ?: true,
            followsEnabled = flags[FeatureFlagKey.FOLLOWS]?.enabled ?: true,
            recommendedFeedEnabled = flags[FeatureFlagKey.RECOMMENDED_FEED]?.enabled ?: true,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    init {
        viewModelScope.launch {
            feedRepository.refreshFeed()
        }
    }

    fun onWatchMilestone(
        postId: String,
        channelId: String,
        type: WatchEventType,
        completion: Float,
        watchedSeconds: Int,
    ) {
        viewModelScope.launch {
            recordWatchEventUseCase(
                WatchEvent(
                    id = idGenerator.newId(prefix = "watch"),
                    userId = "user-me",
                    postId = postId,
                    channelId = channelId,
                    type = type,
                    watchCompletion = completion,
                    watchedSeconds = watchedSeconds,
                    occurredAt = timeProvider.now(),
                ),
            )
        }
    }

    fun onToggleLike(postId: String) {
        viewModelScope.launch {
            togglePostReactionUseCase(postId = postId, kind = ReactionKind.LIKE)
        }
    }

    fun onToggleDislike(postId: String) {
        viewModelScope.launch {
            togglePostReactionUseCase(postId = postId, kind = ReactionKind.DISLIKE)
        }
    }

    fun onToggleSave(postId: String, isSaved: Boolean) {
        viewModelScope.launch {
            toggleSavedPostUseCase(postId = postId, isSaved = isSaved)
        }
    }

    fun onToggleFollow(channelId: String, currentlySubscribed: Boolean) {
        viewModelScope.launch {
            toggleFollowChannelUseCase(channelId = channelId, subscribed = !currentlySubscribed)
        }
    }
}
