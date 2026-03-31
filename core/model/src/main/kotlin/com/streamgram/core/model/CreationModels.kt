package com.streamgram.core.model

enum class ConversationVisibility {
    PRIVATE,
    PUBLIC,
}

data class CreateChatRequest(
    val title: String,
    val description: String,
    val visibility: ConversationVisibility,
    val avatarUri: String? = null,
)

data class CreateChannelRequest(
    val title: String,
    val description: String,
    val handle: String?,
    val visibility: ConversationVisibility,
    val avatarUri: String? = null,
)
