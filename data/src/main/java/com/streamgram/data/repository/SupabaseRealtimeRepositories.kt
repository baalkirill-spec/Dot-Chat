@file:OptIn(io.github.jan.supabase.annotations.SupabaseExperimental::class)

package com.streamgram.data.repository

import com.streamgram.core.common.IdGenerator
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.model.ActivityNotification
import com.streamgram.core.model.BlockedUser
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort
import com.streamgram.core.model.Contact
import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.model.ConversationVisibility
import com.streamgram.core.model.CreateChannelRequest
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.Message
import com.streamgram.core.model.PrivacySettings
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.SecurityNotification
import com.streamgram.core.model.User
import com.streamgram.core.supabase.DotChatSupabaseClient
import com.streamgram.core.supabase.SupabaseActivityNotificationRow
import com.streamgram.core.supabase.SupabaseBlockedUserRow
import com.streamgram.core.supabase.SupabaseChannelRow
import com.streamgram.core.supabase.SupabaseChatMemberRow
import com.streamgram.core.supabase.SupabaseChatMembershipRow
import com.streamgram.core.supabase.SupabaseCommentRow
import com.streamgram.core.supabase.SupabaseContactRow
import com.streamgram.core.supabase.SupabaseDeviceSessionRow
import com.streamgram.core.supabase.SupabaseMessageAttachmentRow
import com.streamgram.core.supabase.SupabaseMessageRow
import com.streamgram.core.supabase.SupabasePrivacySettingsRow
import com.streamgram.core.supabase.SupabaseProfileRow
import com.streamgram.core.supabase.SupabaseSecurityNotificationRow
import com.streamgram.core.supabase.toBlockedUser
import com.streamgram.core.supabase.toChannel
import com.streamgram.core.supabase.toChat
import com.streamgram.core.supabase.toComment
import com.streamgram.core.supabase.toContact
import com.streamgram.core.supabase.toDeviceSession
import com.streamgram.core.supabase.toMessage
import com.streamgram.core.supabase.toNotification
import com.streamgram.core.supabase.toPrivacySettings
import com.streamgram.core.supabase.toSecurityNotification
import com.streamgram.core.supabase.toUser
import com.streamgram.domain.repository.ActivityRepository
import com.streamgram.domain.repository.ChannelsRepository
import com.streamgram.domain.repository.ChatsRepository
import com.streamgram.domain.repository.CommentsRepository
import com.streamgram.domain.repository.ContactsRepository
import com.streamgram.domain.repository.PrivacyRepository
import com.streamgram.domain.repository.SessionsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SupabaseChatInsertRow(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val visibility: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class SupabaseChannelInsertRow(
    val id: String,
    val title: String,
    val handle: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val description: String,
    val topics: List<String> = emptyList(),
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("cover_url") val coverUrl: String? = null,
    val visibility: String,
    @SerialName("invite_link") val inviteLink: String? = null,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
private data class SupabaseChannelSubscriptionRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
private data class SupabaseMessageInsertRow(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("message_type") val messageType: String,
    val text: String,
    val attachments: List<SupabaseMessageAttachmentRow> = emptyList(),
    @SerialName("forwarded_post_id") val forwardedPostId: String? = null,
    @SerialName("reply_to_message_id") val replyToMessageId: String? = null,
    @SerialName("is_outgoing") val isOutgoing: Boolean = true,
    @SerialName("delivery_status") val deliveryStatus: String = "sent",
    @SerialName("read_status") val readStatus: String = "pending",
    @SerialName("created_at") val createdAt: String,
)

@Singleton
class SupabaseContactsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val supabase: DotChatSupabaseClient,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : ContactsRepository {
    override fun observeContacts(): Flow<List<Contact>> {
        return preferences.preferences.flatMapLatest { snapshot ->
            val ownerUserId = snapshot.sessionUserId ?: return@flatMapLatest flowOf(emptyList())
            from(supabase.client, "contacts")
                .selectAsFlow(
                    SupabaseContactRow::id,
                    "contacts-$ownerUserId",
                    FilterOperation("owner_user_id", FilterOperator.EQ, ownerUserId),
                )
                .mapLatest { rows ->
                    val usersById = fetchProfiles(rows.map(SupabaseContactRow::contactUserId).toSet())
                    rows.mapNotNull { row ->
                        usersById[row.contactUserId]?.let(row::toContact)
                    }.sortedBy { it.user.displayName.lowercase() }
                }
                .catch { throwable ->
                    if (throwable.isMissingSupabaseTable("contacts", "profiles")) {
                        emit(emptyList())
                    } else {
                        throw throwable
                    }
                }
        }
    }

    override suspend fun searchUsers(query: String): List<ContactSearchResult> {
        val snapshot = preferences.preferences.first()
        val ownerUserId = snapshot.sessionUserId ?: return emptyList()
        if (query.isBlank()) return emptyList()
        val normalized = query.trim()
        val normalizedDigits = normalized.filter(Char::isDigit)
        val normalizedName = normalized.lowercase()
        val existingContacts = observeContacts().first().map(Contact::contactUserId).toSet()
        val rows = runCatching {
            from(supabase.client, "profiles")
                .select {
                    filter {
                        or {
                            ilike("username", "%$normalized%")
                            ilike("display_name", "%$normalized%")
                            if (normalizedDigits.isNotBlank()) {
                                ilike("phone_number", "%$normalizedDigits%")
                            }
                        }
                        neq("id", ownerUserId)
                    }
                    limit(40)
                }
                .decodeList<SupabaseProfileRow>()
        }.getOrElse { throwable ->
            if (throwable.isMissingSupabaseTable("profiles")) {
                emptyList()
            } else {
                throw throwable
            }
        }
        return rows
            .map { profile -> profile.toUser() }
            .distinctBy(User::id)
            .map { user ->
                val matchedByUsername = user.username.contains(normalized, ignoreCase = true)
                val matchedByPhone = normalizedDigits.isNotBlank() && user.phoneNumber?.contains(normalizedDigits) == true
                val score = user.searchScore(query = normalizedName, digits = normalizedDigits)
                score to ContactSearchResult(
                    user = user,
                    matchedByUsername = matchedByUsername,
                    matchedByPhone = matchedByPhone,
                    alreadyInContacts = user.id in existingContacts,
                )
            }
            .filter { (score, result) -> score > 0 || result.matchedByPhone }
            .sortedWith(
                compareByDescending<Pair<Int, ContactSearchResult>> { it.first }
                    .thenBy { it.second.user.displayName.lowercase() },
            )
            .take(20)
            .map { it.second }
    }

    override suspend fun addContact(userId: String, alias: String?) {
        val snapshot = preferences.preferences.first()
        val ownerUserId = snapshot.sessionUserId ?: return
        from(supabase.client, "contacts").upsert(
            SupabaseContactRow(
                id = idGenerator.newId("contact"),
                ownerUserId = ownerUserId,
                contactUserId = userId,
                localAlias = alias,
                isFavorite = false,
                createdAt = timeProvider.now().toString(),
                updatedAt = timeProvider.now().toString(),
            ),
        )
    }

    override suspend fun removeContact(userId: String) {
        val snapshot = preferences.preferences.first()
        val ownerUserId = snapshot.sessionUserId ?: return
        from(supabase.client, "contacts").delete {
            filter {
                eq("owner_user_id", ownerUserId)
                eq("contact_user_id", userId)
            }
        }
    }

    override suspend fun startChatWithContact(userId: String): String {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return ""
        val existing = from(supabase.client, "chat_members")
            .select {
                filter {
                    eq("user_id", currentUserId)
                }
            }
            .decodeList<SupabaseChatMemberRow>()
        val matchingChatId = existing
            .groupBy(SupabaseChatMemberRow::chatId)
            .keys
            .firstOrNull { chatId ->
                val members = from(supabase.client, "chat_members")
                    .select {
                        filter { eq("chat_id", chatId) }
                    }
                    .decodeList<SupabaseChatMemberRow>()
                members.map(SupabaseChatMemberRow::userId).toSet() == setOf(currentUserId, userId)
            }
        if (matchingChatId != null) return matchingChatId

        val target = fetchProfiles(setOf(userId))[userId] ?: return ""
        val chatId = idGenerator.newId("chat")
        val now = timeProvider.now().toString()
        from(supabase.client, "chats").insert(
            SupabaseChatInsertRow(
                id = chatId,
                title = target.displayName,
                description = "",
                visibility = "private",
                createdBy = currentUserId,
                createdAt = now,
                updatedAt = now,
            ),
        )
        from(supabase.client, "chat_members").insert(
            listOf(
                SupabaseChatMemberRow(
                    id = idGenerator.newId("member"),
                    chatId = chatId,
                    userId = currentUserId,
                    joinedAt = now,
                ),
                SupabaseChatMemberRow(
                    id = idGenerator.newId("member"),
                    chatId = chatId,
                    userId = userId,
                    joinedAt = now,
                ),
            ),
        )
        runCatching {
            from(supabase.client, "chat_memberships").insert(
                SupabaseChatMembershipRow(
                    id = idGenerator.newId("membership"),
                    chatId = chatId,
                    userId = currentUserId,
                    title = target.displayName,
                    description = target.bio,
                    avatarUrl = target.avatarUrl.takeIf { it.isNotBlank() },
                    visibility = "private",
                    lastActivityAt = now,
                ),
            )
        }.getOrElse { throwable ->
            if (!throwable.isMissingSupabaseTable("chat_memberships")) throw throwable
        }
        return chatId
    }

    private suspend fun fetchProfiles(userIds: Set<String>): Map<String, User> {
        if (userIds.isEmpty()) return emptyMap()
        return from(supabase.client, "profiles")
            .select {
                filter { isIn("id", userIds.toList()) }
            }
            .decodeList<SupabaseProfileRow>()
            .associateBy(SupabaseProfileRow::id) { it.toUser() }
    }
}

@Singleton
class SupabaseChatsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val supabase: DotChatSupabaseClient,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : ChatsRepository {
    override fun observeChats(): Flow<List<Chat>> {
        return preferences.preferences.flatMapLatest { snapshot ->
            val currentUserId = snapshot.sessionUserId ?: return@flatMapLatest flowOf(emptyList())
            from(supabase.client, "chat_memberships")
                .selectAsFlow(
                    SupabaseChatMembershipRow::id,
                    "chat-memberships-$currentUserId",
                    FilterOperation("user_id", FilterOperator.EQ, currentUserId),
                )
                .mapLatest { rows ->
                    val membersByChat = fetchMembers(rows.map(SupabaseChatMembershipRow::chatId).toSet())
                    rows.sortedByDescending { it.lastActivityAt }.map { row ->
                        row.toChat(membersByChat[row.chatId].orEmpty())
                    }
                }
                .catch { throwable ->
                    if (throwable.isMissingSupabaseTable("chat_memberships")) {
                        emitAll(observeChatsWithoutMemberships(currentUserId))
                    } else {
                        throw throwable
                    }
                }
        }
    }

    override fun observeMessages(chatId: String): Flow<List<Message>> {
        return preferences.preferences.flatMapLatest { snapshot ->
            val currentUserId = snapshot.sessionUserId ?: return@flatMapLatest flowOf(emptyList())
            from(supabase.client, "messages")
                .selectAsFlow(
                    SupabaseMessageRow::id,
                    "messages-$chatId",
                    FilterOperation("chat_id", FilterOperator.EQ, chatId),
                )
                .mapLatest { rows ->
                    val senders = fetchProfiles(rows.map(SupabaseMessageRow::senderId).toSet() + currentUserId)
                    rows.sortedBy { it.createdAt }.map { row ->
                        row.toMessage(senders[row.senderId] ?: unknownUser(row.senderId))
                    }
                }
                .catch { throwable ->
                    if (throwable.isMissingSupabaseTable("messages", "profiles")) {
                        emit(emptyList())
                    } else {
                        throw throwable
                    }
                }
        }
    }

    override suspend fun createChat(request: CreateChatRequest): String {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return ""
        val chatId = idGenerator.newId("chat")
        val membershipId = idGenerator.newId("membership")
        val memberId = idGenerator.newId("member")
        val now = timeProvider.now().toString()
        val visibility = request.visibility.toBackendVisibility()

        from(supabase.client, "chats").insert(
            SupabaseChatInsertRow(
                id = chatId,
                title = request.title,
                description = request.description,
                avatarUrl = request.avatarUri,
                visibility = visibility,
                createdBy = currentUserId,
                createdAt = now,
                updatedAt = now,
            ),
        )
        from(supabase.client, "chat_members").insert(
            SupabaseChatMemberRow(
                id = memberId,
                chatId = chatId,
                userId = currentUserId,
                joinedAt = now,
            ),
        )
        runCatching {
            from(supabase.client, "chat_memberships").insert(
                SupabaseChatMembershipRow(
                    id = membershipId,
                    chatId = chatId,
                    userId = currentUserId,
                    title = request.title,
                    description = request.description,
                    avatarUrl = request.avatarUri,
                    visibility = visibility,
                    lastActivityAt = now,
                ),
            )
        }.getOrElse { throwable ->
            if (!throwable.isMissingSupabaseTable("chat_memberships")) throw throwable
        }
        return chatId
    }

    override suspend fun sendMessage(chatId: String, text: String) {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return
        val now = timeProvider.now().toString()
        from(supabase.client, "messages").insert(
            SupabaseMessageInsertRow(
                id = idGenerator.newId("message"),
                chatId = chatId,
                senderId = currentUserId,
                messageType = "text",
                text = text,
                createdAt = now,
            ),
        )
        runCatching {
            from(supabase.client, "chat_memberships").update(
                mapOf(
                    "last_message_preview" to text,
                    "last_activity_at" to now,
                ),
            ) {
                filter {
                    eq("chat_id", chatId)
                    eq("user_id", currentUserId)
                }
            }
        }.getOrElse { throwable ->
            if (!throwable.isMissingSupabaseTable("chat_memberships")) throw throwable
        }
    }

    override suspend fun forwardPost(postId: String, chatId: String, note: String?) {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return
        val now = timeProvider.now().toString()
        from(supabase.client, "messages").insert(
            SupabaseMessageInsertRow(
                id = idGenerator.newId("message"),
                chatId = chatId,
                senderId = currentUserId,
                messageType = "forwarded_post",
                text = note.orEmpty(),
                forwardedPostId = postId,
                createdAt = now,
            ),
        )
    }

    private suspend fun fetchMembers(chatIds: Set<String>): Map<String, List<User>> {
        if (chatIds.isEmpty()) return emptyMap()
        val memberRows = from(supabase.client, "chat_members")
            .select {
                filter { isIn("chat_id", chatIds.toList()) }
            }
            .decodeList<SupabaseChatMemberRow>()
        val profiles = fetchProfiles(memberRows.map(SupabaseChatMemberRow::userId).toSet())
        return memberRows.groupBy(SupabaseChatMemberRow::chatId).mapValues { (_, rows) ->
            rows.mapNotNull { profiles[it.userId] }
        }
    }

    private suspend fun fetchProfiles(userIds: Set<String>): Map<String, User> {
        if (userIds.isEmpty()) return emptyMap()
        return from(supabase.client, "profiles")
            .select {
                filter { isIn("id", userIds.toList()) }
            }
            .decodeList<SupabaseProfileRow>()
            .associateBy(SupabaseProfileRow::id) { it.toUser() }
    }

    private fun unknownUser(userId: String): User {
        return User(
            id = userId,
            handle = userId,
            displayName = userId,
            avatarUrl = "",
            bio = "",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
        )
    }

    private fun observeChatsWithoutMemberships(currentUserId: String): Flow<List<Chat>> {
        return from(supabase.client, "chat_members")
            .selectAsFlow(
                SupabaseChatMemberRow::id,
                "chat-members-$currentUserId",
                FilterOperation("user_id", FilterOperator.EQ, currentUserId),
            )
            .mapLatest { membershipRows ->
                val chatIds = membershipRows.map(SupabaseChatMemberRow::chatId).toSet()
                if (chatIds.isEmpty()) return@mapLatest emptyList()
                val chats = from(supabase.client, "chats")
                    .select {
                        filter { isIn("id", chatIds.toList()) }
                    }
                    .decodeList<SupabaseChatInsertRow>()
                val membersByChat = fetchMembers(chatIds)
                chats.sortedByDescending { it.updatedAt }.map { row ->
                    Chat(
                        id = row.id,
                        title = row.title,
                        description = row.description,
                        avatarUrl = row.avatarUrl,
                        visibility = if (row.visibility.equals("public", ignoreCase = true)) {
                            ConversationVisibility.PUBLIC
                        } else {
                            ConversationVisibility.PRIVATE
                        },
                        members = membersByChat[row.id].orEmpty(),
                        unreadCount = 0,
                        isMuted = false,
                        lastMessagePreview = "",
                        lastActivityAt = row.updatedAt.toInstantOrNow(),
                    )
                }
            }
            .catch { fallbackError ->
                if (fallbackError.isMissingSupabaseTable("chat_members", "chats", "profiles")) {
                    emit(emptyList())
                } else {
                    throw fallbackError
                }
            }
    }
}

@Singleton
class SupabaseChannelsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val supabase: DotChatSupabaseClient,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : ChannelsRepository {
    override fun observeDiscoverChannels(): Flow<List<Channel>> {
        return preferences.preferences.flatMapLatest { snapshot ->
            val userId = snapshot.sessionUserId
            val channelsFlow = from(supabase.client, "channels")
                .selectAsFlow(SupabaseChannelRow::id, "channels-directory")
            if (userId == null) {
                channelsFlow.map { rows -> rows.map(SupabaseChannelRow::toChannel) }
            } else {
                val subscriptionsFlow = from(supabase.client, "channel_subscriptions")
                    .selectAsFlow(
                        SupabaseChannelSubscriptionRow::id,
                        "channel-subscriptions-$userId",
                        FilterOperation("user_id", FilterOperator.EQ, userId),
                    )
                combine(channelsFlow, subscriptionsFlow) { channels, subscriptions ->
                        val subscribedIds = subscriptions.map(SupabaseChannelSubscriptionRow::channelId).toSet()
                        channels.map { row ->
                            row.copy(isSubscribed = row.id in subscribedIds).toChannel()
                        }
                    }
            }
        }.catch { throwable ->
            if (throwable.isMissingSupabaseTable("channels", "channel_subscriptions")) {
                emit(emptyList())
            } else {
                throw throwable
            }
        }
    }

    override fun observeChannel(channelId: String): Flow<Channel?> {
        return observeDiscoverChannels().map { channels -> channels.firstOrNull { it.id == channelId } }
    }

    override suspend fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return emptyList()
        val normalized = query.trim()
        return runCatching {
            from(supabase.client, "channels")
                .select {
                    filter {
                        or {
                            ilike("title", "%$normalized%")
                            ilike("handle", "%$normalized%")
                            ilike("description", "%$normalized%")
                        }
                    }
                    limit(20)
                }
                .decodeList<SupabaseChannelRow>()
                .map(SupabaseChannelRow::toChannel)
        }.getOrElse { throwable ->
            if (throwable.isMissingSupabaseTable("channels")) emptyList() else throw throwable
        }
    }

    override suspend fun createChannel(request: CreateChannelRequest): String {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return ""
        val channelId = idGenerator.newId("channel")
        from(supabase.client, "channels").insert(
            SupabaseChannelInsertRow(
                id = channelId,
                title = request.title,
                handle = request.handle.orEmpty(),
                avatarUrl = request.avatarUri,
                description = request.description,
                visibility = request.visibility.toBackendVisibility(),
                createdBy = currentUserId,
                inviteLink = request.handle?.takeIf { request.visibility == ConversationVisibility.PRIVATE }
                    ?.let { "dotchat://invite/channel/$it" },
            ),
        )
        from(supabase.client, "channel_subscriptions").upsert(
            SupabaseChannelSubscriptionRow(
                id = idGenerator.newId("subscription"),
                userId = currentUserId,
                channelId = channelId,
                createdAt = timeProvider.now().toString(),
            ),
        )
        return channelId
    }

    override suspend fun setSubscribed(channelId: String, subscribed: Boolean) {
        val snapshot = preferences.preferences.first()
        val currentUserId = snapshot.sessionUserId ?: return
        if (subscribed) {
            from(supabase.client, "channel_subscriptions").upsert(
                SupabaseChannelSubscriptionRow(
                    id = idGenerator.newId("subscription"),
                    userId = currentUserId,
                    channelId = channelId,
                    createdAt = timeProvider.now().toString(),
                ),
            )
        } else {
            from(supabase.client, "channel_subscriptions").delete {
                filter {
                    eq("user_id", currentUserId)
                    eq("channel_id", channelId)
                }
            }
        }
    }
}

@Singleton
class SupabaseActivityRepository @Inject constructor(
    private val supabase: DotChatSupabaseClient,
) : ActivityRepository {
    override fun observeActivity(): Flow<List<ActivityNotification>> {
        return from(supabase.client, "activity_notifications")
            .selectAsFlow(SupabaseActivityNotificationRow::id, "activity-notifications")
            .map { rows -> rows.sortedByDescending { it.createdAt }.map(SupabaseActivityNotificationRow::toNotification) }
            .catch { throwable ->
                if (throwable.isMissingSupabaseTable("activity_notifications")) {
                    emit(emptyList())
                } else {
                    throw throwable
                }
            }
    }
}

@Singleton
class SupabasePrivacyRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val supabase: DotChatSupabaseClient,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : PrivacyRepository {
    override fun observePrivacySettings(): Flow<PrivacySettings> {
        return preferences.preferences.flatMapLatest { snapshot ->
            val userId = snapshot.sessionUserId ?: return@flatMapLatest flowOf(defaultPrivacySettings())
            from(supabase.client, "privacy_settings")
                .selectAsFlow(
                    SupabasePrivacySettingsRow::userId,
                    "privacy-$userId",
                    FilterOperation("user_id", FilterOperator.EQ, userId),
                )
                .map { rows -> rows.firstOrNull()?.toPrivacySettings() ?: defaultPrivacySettings() }
                .catch { throwable ->
                    if (throwable.isMissingSupabaseTable("privacy_settings")) {
                        emit(defaultPrivacySettings())
                    } else {
                        throw throwable
                    }
                }
        }
    }

    override fun observeBlockedUsers(): Flow<List<BlockedUser>> {
        return from(supabase.client, "blocked_users")
            .selectAsFlow(SupabaseBlockedUserRow::id, "blocked-users")
            .map { rows -> rows.map(SupabaseBlockedUserRow::toBlockedUser) }
            .catch { throwable ->
                if (throwable.isMissingSupabaseTable("blocked_users")) {
                    emit(emptyList())
                } else {
                    throw throwable
                }
            }
    }

    override suspend fun updatePrivacySettings(settings: PrivacySettings) {
        val snapshot = preferences.preferences.first()
        val userId = snapshot.sessionUserId ?: return
        from(supabase.client, "privacy_settings").upsert(
            SupabasePrivacySettingsRow(
                userId = userId,
                phoneNumberVisibility = settings.phoneNumberVisibility.name.lowercase(),
                usernameVisibility = settings.usernameVisibility.name.lowercase(),
                avatarVisibility = settings.avatarVisibility.name.lowercase(),
                lastSeenVisibility = settings.lastSeenVisibility.name.lowercase(),
                bioVisibility = settings.bioVisibility.name.lowercase(),
                whoCanAddToChats = settings.whoCanAddToChats.name.lowercase(),
                whoCanCall = settings.whoCanCall.name.lowercase(),
                whoCanMessage = settings.whoCanMessage.name.lowercase(),
            ),
        )
    }

    override suspend fun blockUser(userId: String, reason: String?) {
        from(supabase.client, "blocked_users").insert(
            SupabaseBlockedUserRow(
                id = idGenerator.newId("block"),
                userId = userId,
                reason = reason,
                createdAt = timeProvider.now().toString(),
            ),
        )
    }

    override suspend fun unblockUser(userId: String) {
        from(supabase.client, "blocked_users").delete {
            filter { eq("user_id", userId) }
        }
    }
}

@Singleton
class SupabaseSessionsRepository @Inject constructor(
    private val supabase: DotChatSupabaseClient,
) : SessionsRepository {
    override fun observeSessions(): Flow<List<DeviceSession>> {
        return from(supabase.client, "device_sessions")
            .selectAsFlow(SupabaseDeviceSessionRow::id, "device-sessions")
            .map { rows -> rows.sortedByDescending { it.lastActiveAt }.map(SupabaseDeviceSessionRow::toDeviceSession) }
            .catch { throwable ->
                if (throwable.isMissingSupabaseTable("device_sessions")) {
                    emit(emptyList())
                } else {
                    throw throwable
                }
            }
    }

    override fun observeSecurityNotifications(): Flow<List<SecurityNotification>> {
        return from(supabase.client, "security_notifications")
            .selectAsFlow(SupabaseSecurityNotificationRow::id, "security-events")
            .map { rows -> rows.sortedByDescending { it.createdAt }.map(SupabaseSecurityNotificationRow::toSecurityNotification) }
            .catch { throwable ->
                if (throwable.isMissingSupabaseTable("security_notifications")) {
                    emit(emptyList())
                } else {
                    throw throwable
                }
            }
    }

    override suspend fun terminateSession(sessionId: String) {
        from(supabase.client, "device_sessions").delete {
            filter { eq("id", sessionId) }
        }
    }

    override suspend fun terminateOtherSessions() {
        from(supabase.client, "device_sessions").delete {
            filter { eq("is_current", false) }
        }
    }
}

@Singleton
class SupabaseCommentsRepository @Inject constructor(
    private val supabase: DotChatSupabaseClient,
    private val preferences: UserPreferencesDataSource,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : CommentsRepository {
    override fun observeComments(postId: String, sort: CommentSort): Flow<List<Comment>> {
        return from(supabase.client, "comments")
            .selectAsFlow(
                SupabaseCommentRow::id,
                "comments-$postId",
                FilterOperation("post_id", FilterOperator.EQ, postId),
            )
            .mapLatest { rows ->
                val authors = from(supabase.client, "profiles")
                    .select {
                        filter { isIn("id", rows.map(SupabaseCommentRow::authorId).distinct()) }
                    }
                    .decodeList<SupabaseProfileRow>()
                    .associateBy(SupabaseProfileRow::id) { it.toUser() }
                val mapped = rows.mapNotNull { row ->
                    authors[row.authorId]?.let { author -> row.toComment(author) }
                }
                when (sort) {
                    CommentSort.TOP -> mapped.sortedByDescending { it.replyCount + it.reactions.likeCount }
                    CommentSort.NEW -> mapped.sortedByDescending { it.createdAt }
                }
            }
            .catch { throwable ->
                if (throwable.isMissingSupabaseTable("comments", "profiles")) {
                    emit(emptyList())
                } else {
                    throw throwable
                }
            }
    }

    override suspend fun addComment(postId: String, message: String, parentCommentId: String?) {
        val userId = preferences.preferences.first().sessionUserId ?: return
        from(supabase.client, "comments").insert(
            SupabaseCommentRow(
                id = idGenerator.newId("comment"),
                postId = postId,
                authorId = userId,
                message = message,
                parentCommentId = parentCommentId,
                createdAt = timeProvider.now().toString(),
            ),
        )
    }

    override suspend fun react(commentId: String, kind: ReactionKind, emoji: String?) {
        val current = from(supabase.client, "comments")
            .select {
                filter { eq("id", commentId) }
                single()
            }
            .decodeSingleOrNull<SupabaseCommentRow>() ?: return
        val updated = when (kind) {
            ReactionKind.LIKE -> current.copy(likeCount = current.likeCount + 1, viewerReaction = "like")
            ReactionKind.DISLIKE -> current.copy(dislikeCount = current.dislikeCount + 1, viewerReaction = "dislike")
            ReactionKind.EMOJI -> current.copy(viewerReaction = emoji)
        }
        from(supabase.client, "comments").update(updated) {
            filter { eq("id", commentId) }
        }
    }
}

private fun ConversationVisibility.toBackendVisibility(): String {
    return if (this == ConversationVisibility.PUBLIC) "public" else "private"
}

private fun from(client: SupabaseClient, table: String) = client.from(table)

private fun defaultPrivacySettings(): PrivacySettings {
    return PrivacySettings(
        phoneNumberVisibility = com.streamgram.core.model.PrivacyAudience.CONTACTS,
        usernameVisibility = com.streamgram.core.model.PrivacyAudience.EVERYONE,
        avatarVisibility = com.streamgram.core.model.PrivacyAudience.EVERYONE,
        lastSeenVisibility = com.streamgram.core.model.PrivacyAudience.CONTACTS,
        bioVisibility = com.streamgram.core.model.PrivacyAudience.EVERYONE,
        whoCanAddToChats = com.streamgram.core.model.PrivacyAudience.CONTACTS,
        whoCanCall = com.streamgram.core.model.PrivacyAudience.CONTACTS,
        whoCanMessage = com.streamgram.core.model.PrivacyAudience.EVERYONE,
    )
}

private fun Throwable.isMissingSupabaseTable(vararg tables: String): Boolean {
    val exception = this as? PostgrestRestException ?: return false
    val description = buildString {
        append(exception.message.orEmpty())
        append(' ')
        append(exception.error)
    }
    val looksLikeMissingTable =
        description.contains("schema cache", ignoreCase = true) ||
            description.contains("PGRST205", ignoreCase = true) ||
            description.contains("Could not find the table", ignoreCase = true)
    if (!looksLikeMissingTable) return false
    if (tables.isEmpty()) return true
    return tables.any { table ->
        description.contains("public.$table", ignoreCase = true) ||
            description.contains("/$table", ignoreCase = true) ||
            description.contains(table, ignoreCase = true)
    }
}

private fun String.toInstantOrNow(): java.time.Instant =
    runCatching { java.time.Instant.parse(this) }.getOrElse { java.time.Instant.now() }

private fun User.searchScore(
    query: String,
    digits: String,
): Int {
    val usernameValue = username.lowercase()
    val displayValue = displayName.lowercase()
    val phoneValue = phoneNumber.orEmpty().filter(Char::isDigit)
    return when {
        usernameValue == query -> 120
        displayValue == query -> 110
        usernameValue.startsWith(query) -> 95
        displayValue.startsWith(query) -> 85
        usernameValue.contains(query) -> 70
        displayValue.contains(query) -> 60
        digits.isNotBlank() && phoneValue.startsWith(digits) -> 55
        digits.isNotBlank() && phoneValue.contains(digits) -> 45
        else -> 0
    }
}
