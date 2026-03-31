package com.streamgram.domain.repository

import com.streamgram.core.model.Chat
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.core.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {
    fun observeChats(): Flow<List<Chat>>
    fun observeMessages(chatId: String): Flow<List<Message>>
    suspend fun createChat(request: CreateChatRequest): String
    suspend fun sendMessage(chatId: String, text: String)
    suspend fun forwardPost(postId: String, chatId: String, note: String? = null)
}
