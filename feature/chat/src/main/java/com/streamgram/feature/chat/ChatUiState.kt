package com.streamgram.feature.chat

import com.streamgram.core.model.Chat
import com.streamgram.core.model.Message

data class ChatUiState(
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val callTargetUserId: String? = null,
)

data class ForwardPostUiState(
    val postId: String = "",
    val chats: List<Chat> = emptyList(),
    val forwardedChatId: String? = null,
)
