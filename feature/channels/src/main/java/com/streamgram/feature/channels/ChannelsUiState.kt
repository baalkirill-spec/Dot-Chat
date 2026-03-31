package com.streamgram.feature.channels

import com.streamgram.core.model.Channel

data class ChannelsUiState(
    val channels: List<Channel> = emptyList(),
)

data class ChannelProfileUiState(
    val channel: Channel? = null,
)
