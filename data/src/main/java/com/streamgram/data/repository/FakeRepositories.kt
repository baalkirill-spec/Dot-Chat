package com.streamgram.data.repository

import com.streamgram.core.common.AppResult
import com.streamgram.core.model.ActivityNotification
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort
import com.streamgram.core.model.Contact
import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.model.CreateChannelRequest
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.core.model.FeedItem
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.RecommendationProfile
import com.streamgram.core.model.User
import com.streamgram.core.model.WatchEvent
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.telemetry.FeedWatchTelemetryEvent
import com.streamgram.core.telemetry.TelemetryLogger
import com.streamgram.data.fake.AppOnlyTransportGateway
import com.streamgram.data.fake.FakeStreamGramStore
import com.streamgram.data.fake.RecommendationScorer
import com.streamgram.domain.repository.ActivityRepository
import com.streamgram.domain.repository.ChannelsRepository
import com.streamgram.domain.repository.ChatsRepository
import com.streamgram.domain.repository.CommentsRepository
import com.streamgram.domain.repository.ContactsRepository
import com.streamgram.domain.repository.FeedRepository
import com.streamgram.domain.repository.RecommendationRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class FakeFeedRepository @Inject constructor(
    private val store: FakeStreamGramStore,
    private val scorer: RecommendationScorer,
    private val telemetryLogger: TelemetryLogger,
    private val gateway: AppOnlyTransportGateway,
) : FeedRepository {
    override fun observeFeed(): Flow<List<FeedItem>> {
        return combine(store.posts, store.channels, store.recommendationProfile) { posts, channels, profile ->
            scorer.rank(posts = posts, channelsById = channels.associateBy(Channel::id), profile = profile)
        }
    }

    override suspend fun refreshFeed(): AppResult<Unit> {
        gateway.execute(feature = NetworkFeature.FEED, path = "/v1/feed")
        return AppResult.Success(Unit)
    }

    override suspend fun recordWatchEvent(event: WatchEvent) {
        store.recordWatchEvent(event)
        telemetryLogger.log(FeedWatchTelemetryEvent(event))
    }

    override suspend fun toggleReaction(postId: String, kind: ReactionKind, emoji: String?) {
        store.toggleReaction(postId = postId, kind = kind, emoji = emoji)
    }

    override suspend fun setSaved(postId: String, isSaved: Boolean) {
        store.setSaved(postId = postId, isSaved = isSaved)
    }
}

@Singleton
class FakeChannelsRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : ChannelsRepository {
    override fun observeDiscoverChannels(): Flow<List<Channel>> = store.channels

    override fun observeChannel(channelId: String): Flow<Channel?> {
        return store.channels.map { channels -> channels.firstOrNull { it.id == channelId } }
    }

    override suspend fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return emptyList()
        return store.channels.first().filter { channel ->
            channel.title.contains(query, ignoreCase = true) ||
                channel.handle.contains(query, ignoreCase = true)
        }
    }

    override suspend fun createChannel(request: CreateChannelRequest): String {
        return store.createChannel(request)
    }

    override suspend fun setSubscribed(channelId: String, subscribed: Boolean) {
        store.setSubscribed(channelId = channelId, subscribed = subscribed)
    }
}

@Singleton
class FakeCommentsRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : CommentsRepository {
    override fun observeComments(postId: String, sort: CommentSort): Flow<List<Comment>> {
        return store.comments.map { comments ->
            when (sort) {
                CommentSort.TOP -> comments[postId].orEmpty().sortedByDescending { it.reactions.likeCount + it.replyCount * 3 }
                CommentSort.NEW -> comments[postId].orEmpty().sortedByDescending(Comment::createdAt)
            }
        }
    }

    override suspend fun addComment(postId: String, message: String, parentCommentId: String?) {
        store.addComment(postId = postId, message = message, parentCommentId = parentCommentId)
    }

    override suspend fun react(commentId: String, kind: ReactionKind, emoji: String?) {
        store.reactToComment(commentId = commentId, kind = kind, emoji = emoji)
    }
}

@Singleton
class FakeChatsRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : ChatsRepository {
    override fun observeChats(): Flow<List<Chat>> = store.chats

    override fun observeMessages(chatId: String) = store.messages.map { messages -> messages[chatId].orEmpty() }

    override suspend fun createChat(request: CreateChatRequest): String {
        return store.createChat(request)
    }

    override suspend fun sendMessage(chatId: String, text: String) {
        store.sendMessage(chatId = chatId, text = text)
    }

    override suspend fun forwardPost(postId: String, chatId: String, note: String?) {
        store.forwardPost(postId = postId, chatId = chatId, note = note)
    }
}

@Singleton
class FakeContactsRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : ContactsRepository {
    private val contactsState = MutableStateFlow(
        store.friends.map { friend ->
            Contact(
                ownerUserId = store.currentUser.id,
                contactUserId = friend.id,
                user = User(
                    id = friend.id,
                    handle = friend.displayName.lowercase().replace(" ", ""),
                    displayName = friend.displayName,
                    avatarUrl = friend.avatarUrl,
                    bio = "",
                    subscriptionCount = 0,
                    chatCount = 0,
                    savedPostCount = 0,
                    phoneNumber = null,
                ),
                createdAt = friend.lastSeenAt,
                updatedAt = friend.lastSeenAt,
            )
        },
    )

    private val directoryUsers = listOf(
        store.currentUser.copy(phoneNumber = "+1 555 010 200"),
        User(
            id = "user-sasha",
            handle = "sasha",
            displayName = "Sasha Vale",
            avatarUrl = "https://picsum.photos/id/1011/200/200",
            bio = "Community lead",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
            username = "sashavale",
            phoneNumber = "+1 555 222 030",
        ),
        User(
            id = "user-lina",
            handle = "lina",
            displayName = "Lina Frost",
            avatarUrl = "https://picsum.photos/id/1025/200/200",
            bio = "Support specialist",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
            username = "linafrost",
            phoneNumber = "+1 555 111 020",
        ),
    )

    override fun observeContacts(): Flow<List<Contact>> = contactsState

    override suspend fun searchUsers(query: String): List<ContactSearchResult> {
        if (query.isBlank()) return emptyList()
        val normalized = query.trim()
        val knownIds = contactsState.value.map(Contact::contactUserId).toSet()
        return directoryUsers
            .filter { user ->
                user.id != store.currentUser.id &&
                    (user.username.contains(normalized, ignoreCase = true) ||
                        user.displayName.contains(normalized, ignoreCase = true) ||
                        user.phoneNumber?.contains(normalized.filter(Char::isDigit)) == true)
            }
            .map { user ->
                ContactSearchResult(
                    user = user,
                    matchedByUsername = user.username.contains(normalized, ignoreCase = true),
                    matchedByPhone = user.phoneNumber?.contains(normalized.filter(Char::isDigit)) == true,
                    alreadyInContacts = user.id in knownIds,
                )
            }
    }

    override suspend fun addContact(userId: String, alias: String?) {
        val user = directoryUsers.firstOrNull { it.id == userId } ?: return
        if (contactsState.value.any { it.contactUserId == userId }) return
        val now = Instant.now()
        contactsState.value = contactsState.value + Contact(
            ownerUserId = store.currentUser.id,
            contactUserId = userId,
            user = user,
            localAlias = alias,
            createdAt = now,
            updatedAt = now,
        )
    }

    override suspend fun removeContact(userId: String) {
        contactsState.value = contactsState.value.filterNot { it.contactUserId == userId }
    }

    override suspend fun startChatWithContact(userId: String): String {
        return store.chats.value.firstOrNull { chat -> chat.members.any { it.id == userId } }?.id
            ?: "chat-direct-$userId"
    }
}

@Singleton
class FakeActivityRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : ActivityRepository {
    override fun observeActivity(): Flow<List<ActivityNotification>> = store.activity
}

@Singleton
class FakeRecommendationRepository @Inject constructor(
    private val store: FakeStreamGramStore,
) : RecommendationRepository {
    override fun observeProfile(): Flow<RecommendationProfile> = store.recommendationProfile

    override suspend fun refreshProfile() = Unit
}
