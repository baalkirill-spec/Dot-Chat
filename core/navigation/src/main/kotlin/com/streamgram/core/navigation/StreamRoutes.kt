package com.streamgram.core.navigation

object RootRoutes {
    const val Splash = "root/splash"
    const val Onboarding = "root/onboarding"
    const val Auth = "root/auth"
    const val Main = "root/main"
}

object MainRoutes {
    // ── Tab-level destinations (3 tabs) ──────────────────────────────────
    const val Chats = "main/chats"
    const val Channels = "main/channels"
    const val Account = "main/account"

    // ── Secondary destinations ────────────────────────────────────────────
    const val Notifications = "main/notifications"
    const val Settings = "main/settings"
    const val Privacy = "main/privacy"
    const val Sessions = "main/sessions"
    const val CreateChat = "main/create-chat"
    const val CreateChannel = "main/create-channel"
    const val CreateGroup = "main/create-group"
    const val DeleteAccount = "main/delete-account"
    const val Language = "main/language"
    const val Calls = "main/calls"
    const val MediaViewer = "main/media-viewer"

    // ── Parameterized patterns ────────────────────────────────────────────
    const val ChatDetailPattern = "main/chat/{chatId}"
    const val CommentsPattern = "main/comments/{postId}"
    const val ChannelDetailPattern = "main/channel/{channelId}"
    const val ForwardPattern = "main/forward/{postId}"
    const val CallJoinPattern = "main/calls/join/{roomId}/{type}"

    // ── Deprecated aliases ────────────────────────────────────────────────
    @Deprecated("Contacts tab removed. People search is inside Chats.", ReplaceWith("Chats"))
    const val Contacts = "main/chats"
    @Deprecated("Renamed to Channels", ReplaceWith("Channels"))
    const val Spaces = "main/channels"
    @Deprecated("Renamed to Account", ReplaceWith("Account"))
    const val Profile = "main/account"

    fun chatDetail(chatId: String): String = "main/chat/$chatId"
    fun comments(postId: String): String = "main/comments/$postId"
    fun channelDetail(channelId: String): String = "main/channel/$channelId"
    fun forward(postId: String): String = "main/forward/$postId"
    fun joinCall(roomId: String, type: String): String = "main/calls/join/$roomId/$type"
}
