package com.streamgram.feature.createchat

import com.streamgram.core.model.ConversationVisibility

data class CreateChatUiState(
    val title: String = "",
    val description: String = "",
    val visibility: ConversationVisibility = ConversationVisibility.PUBLIC,
    val isSubmitting: Boolean = false,
    val createdChatId: String? = null,
)
