package com.streamgram.feature.feed

import com.streamgram.core.model.FeedItem

data class FeedUiState(
    val items: List<FeedItem> = emptyList(),
    val autoplayEnabled: Boolean = true,
    val commentsEnabled: Boolean = true,
    val reactionsEnabled: Boolean = true,
    val followsEnabled: Boolean = true,
    val recommendedFeedEnabled: Boolean = true,
    val isLoading: Boolean = true,
)
