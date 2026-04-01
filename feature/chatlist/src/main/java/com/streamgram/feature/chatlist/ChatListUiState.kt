package com.streamgram.feature.chatlist

import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.ContactSearchResult

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val peopleResults: List<ContactSearchResult> = emptyList(),
    val channelResults: List<Channel> = emptyList(),
) {
    val hasSearchResults: Boolean
        get() = query.isNotBlank() && (peopleResults.isNotEmpty() || channelResults.isNotEmpty())
}
