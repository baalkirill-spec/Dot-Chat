package com.streamgram.domain.repository

import com.streamgram.core.model.Channel
import com.streamgram.core.model.CreateChannelRequest
import kotlinx.coroutines.flow.Flow

interface ChannelsRepository {
    fun observeDiscoverChannels(): Flow<List<Channel>>
    fun observeChannel(channelId: String): Flow<Channel?>
    suspend fun searchChannels(query: String): List<Channel>
    suspend fun createChannel(request: CreateChannelRequest): String
    suspend fun setSubscribed(channelId: String, subscribed: Boolean)
}
