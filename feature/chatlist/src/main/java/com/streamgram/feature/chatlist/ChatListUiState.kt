package com.streamgram.feature.chatlist

import com.streamgram.core.model.Chat

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
)
