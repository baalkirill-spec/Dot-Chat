package com.streamgram.domain.usecase

import com.streamgram.core.model.Channel
import com.streamgram.core.model.CreateChannelRequest
import com.streamgram.domain.repository.ChannelsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveDiscoverChannelsUseCase @Inject constructor(
    private val repository: ChannelsRepository,
) {
    operator fun invoke(): Flow<List<Channel>> = repository.observeDiscoverChannels()
}

class ObserveChannelUseCase @Inject constructor(
    private val repository: ChannelsRepository,
) {
    operator fun invoke(channelId: String): Flow<Channel?> = repository.observeChannel(channelId)
}

class ToggleFollowChannelUseCase @Inject constructor(
    private val repository: ChannelsRepository,
) {
    suspend operator fun invoke(channelId: String, subscribed: Boolean) {
        repository.setSubscribed(channelId = channelId, subscribed = subscribed)
    }
}

class CreateChannelUseCase @Inject constructor(
    private val repository: ChannelsRepository,
) {
    suspend operator fun invoke(request: CreateChannelRequest): String {
        return repository.createChannel(request)
    }
}
