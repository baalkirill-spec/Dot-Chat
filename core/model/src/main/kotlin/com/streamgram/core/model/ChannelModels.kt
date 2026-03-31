package com.streamgram.core.model

data class Channel(
    val id: String,
    val title: String,
    val handle: String,
    val avatarUrl: String,
    val description: String,
    val topics: Set<String>,
    val followerCount: Int,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val coverUrl: String?,
    val visibility: ConversationVisibility = ConversationVisibility.PUBLIC,
    val inviteLink: String? = null,
)

data class ChannelSubscription(
    val userId: String,
    val channelId: String,
    val isSubscribed: Boolean,
)
