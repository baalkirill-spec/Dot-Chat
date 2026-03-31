package com.streamgram.core.tdlib.chat

import kotlinx.coroutines.flow.Flow

interface TdLibChatService {
    fun observeChats(): Flow<List<TdLibChatSummary>>
    suspend fun loadMore(limit: Int)
    suspend fun search(query: String): List<TdLibChatSummary>
}

data class TdLibChatSummary(
    val id: Long,
    val title: String,
    val unreadCount: Int,
    val lastMessagePreview: String,
    val isMuted: Boolean,
    val isArchived: Boolean,
)
