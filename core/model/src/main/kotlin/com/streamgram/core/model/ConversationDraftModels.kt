package com.streamgram.core.model

data class ChatPermissions(
    val canInviteMembers: Boolean = true,
    val canSendMessages: Boolean = true,
    val canSendMedia: Boolean = true,
    val canPinMessages: Boolean = false,
    val canEditChatInfo: Boolean = false,
)

enum class Visibility {
    PUBLIC,
    PRIVATE,
}

data class CreateChatDraft(
    val title: String = "",
    val visibility: Visibility = Visibility.PRIVATE,
    val description: String = "",
    val avatarUri: String? = null,
    val participantIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val permissions: ChatPermissions = ChatPermissions(),
    val inviteLinksEnabled: Boolean = true,
    val chatThemeId: String? = null,
)

data class CreateChannelDraft(
    val title: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val description: String = "",
    val avatarUri: String? = null,
    val publicHandle: String = "",
    val commentsEnabled: Boolean = true,
    val inviteLinksEnabled: Boolean = true,
)
