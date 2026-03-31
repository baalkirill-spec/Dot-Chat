package com.streamgram.feature.createchannel

import com.streamgram.core.model.ConversationVisibility

data class CreateChannelUiState(
    val title: String = "",
    val handle: String = "",
    val description: String = "",
    val visibility: ConversationVisibility = ConversationVisibility.PUBLIC,
    val isSubmitting: Boolean = false,
    val createdChannelId: String? = null,
)
