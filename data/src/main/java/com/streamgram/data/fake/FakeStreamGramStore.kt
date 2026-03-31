package com.streamgram.data.fake

import com.streamgram.core.common.IdGenerator
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.model.ActivityNotification
import com.streamgram.core.model.ActivityNotificationKind
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentModerationState
import com.streamgram.core.model.ConversationVisibility
import com.streamgram.core.model.CreateChannelRequest
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.core.model.Friend
import com.streamgram.core.model.MediaAttachment
import com.streamgram.core.model.MessageDeliveryStatus
import com.streamgram.core.model.Message
import com.streamgram.core.model.MessageKind
import com.streamgram.core.model.MessageReadStatus
import com.streamgram.core.model.PostViewerState
import com.streamgram.core.model.ReactionKind
import com.streamgram.core.model.ReactionSummary
import com.streamgram.core.model.User
import com.streamgram.core.model.ViewerReaction
import com.streamgram.core.model.WatchEvent
import com.streamgram.core.model.WatchEventType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeStreamGramStore @Inject constructor(
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) {
    private val initialState = SampleData.initial(now = timeProvider.now())

    val currentUser: User = initialState.currentUser
    val friends: List<Friend> = initialState.friends

    private val _channels = MutableStateFlow(initialState.channels)
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _friends = MutableStateFlow(initialState.friends)
    val friendsFlow: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _posts = MutableStateFlow(initialState.posts)
    val posts = _posts.asStateFlow()

    private val _comments = MutableStateFlow(initialState.comments)
    val comments = _comments.asStateFlow()

    private val _chats = MutableStateFlow(initialState.chats)
    val chats = _chats.asStateFlow()

    private val _messages = MutableStateFlow(initialState.messages)
    val messages = _messages.asStateFlow()

    private val _activity = MutableStateFlow(initialState.activity)
    val activity = _activity.asStateFlow()

    private val _recommendationProfile = MutableStateFlow(initialState.recommendationProfile)
    val recommendationProfile = _recommendationProfile.asStateFlow()

    suspend fun setSubscribed(channelId: String, subscribed: Boolean) {
        _channels.update { channels ->
            channels.map { channel ->
                if (channel.id == channelId) {
                    channel.copy(
                        isSubscribed = subscribed,
                        followerCount = (channel.followerCount + if (subscribed) 1 else -1).coerceAtLeast(0),
                    )
                } else {
                    channel
                }
            }
        }
        _recommendationProfile.update { profile ->
            val followed = profile.followedChannelIds.toMutableSet().apply {
                if (subscribed) add(channelId) else remove(channelId)
            }
            profile.copy(
                followedChannelIds = followed,
                lastUpdatedAt = timeProvider.now(),
            )
        }
        pushActivity(
            title = if (subscribed) "Channel followed" else "Channel unfollowed",
            body = if (subscribed) {
                "The channel was added to your personalized mix."
            } else {
                "The channel was removed from your personalized mix."
            },
            kind = ActivityNotificationKind.FOLLOW,
        )
    }

    suspend fun toggleReaction(postId: String, kind: ReactionKind, emoji: String?) {
        _posts.update { posts ->
            posts.map { post ->
                if (post.id != postId) return@map post
                val currentSummary = post.stats.reactionSummary
                val nextViewerReaction = when {
                    kind == ReactionKind.LIKE && currentSummary.viewerReaction is ViewerReaction.Like -> ViewerReaction.None
                    kind == ReactionKind.DISLIKE && currentSummary.viewerReaction is ViewerReaction.Dislike -> ViewerReaction.None
                    kind == ReactionKind.EMOJI &&
                        currentSummary.viewerReaction is ViewerReaction.Emoji &&
                        (currentSummary.viewerReaction as ViewerReaction.Emoji).value == emoji ->
                        ViewerReaction.None

                    kind == ReactionKind.LIKE -> ViewerReaction.Like
                    kind == ReactionKind.DISLIKE -> ViewerReaction.Dislike
                    else -> ViewerReaction.Emoji(emoji.orEmpty())
                }
                post.copy(stats = post.stats.copy(reactionSummary = updateReactionSummary(currentSummary, nextViewerReaction)))
            }
        }
    }

    suspend fun setSaved(postId: String, isSaved: Boolean) {
        _posts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val delta = when {
                        isSaved && !post.viewerState.isSaved -> 1
                        !isSaved && post.viewerState.isSaved -> -1
                        else -> 0
                    }
                    post.copy(
                        stats = post.stats.copy(savesCount = post.stats.savesCount + delta),
                        viewerState = post.viewerState.copy(isSaved = isSaved),
                    )
                } else {
                    post
                }
            }
        }
    }

    suspend fun recordWatchEvent(event: WatchEvent) {
        val post = _posts.value.firstOrNull { it.id == event.postId } ?: return
        val channel = _channels.value.firstOrNull { it.id == post.channelId } ?: return

        _posts.update { posts ->
            posts.map { existing ->
                if (existing.id == event.postId) {
                    existing.copy(
                        viewerState = existing.viewerState.copy(
                            watchedSeconds = maxOf(existing.viewerState.watchedSeconds, event.watchedSeconds),
                            watchCompletion = maxOf(existing.viewerState.watchCompletion, event.watchCompletion),
                            hasImpression = existing.viewerState.hasImpression || event.type == WatchEventType.IMPRESSION,
                        ),
                    )
                } else {
                    existing
                }
            }
        }

        val interestDelta = when (event.type) {
            WatchEventType.WATCHED_50_PERCENT -> 0.08
            WatchEventType.WATCHED_95_PERCENT, WatchEventType.COMPLETED -> 0.16
            WatchEventType.LIKED, WatchEventType.SAVED, WatchEventType.SHARED, WatchEventType.SENT_TO_FRIEND -> 0.2
            WatchEventType.SKIPPED, WatchEventType.DISLIKED -> -0.14
            else -> 0.0
        }
        if (interestDelta != 0.0) {
            _recommendationProfile.update { profile ->
                val interests = profile.interests.toMutableMap()
                val negatives = profile.negativeSignals.toMutableMap()
                channel.topics.forEach { topic ->
                    if (interestDelta > 0) {
                        interests[topic] = (interests[topic] ?: 0.0) + interestDelta
                    } else {
                        negatives[topic] = (negatives[topic] ?: 0.0) + interestDelta.absoluteValue()
                    }
                }
                profile.copy(
                    interests = interests,
                    negativeSignals = negatives,
                    lastUpdatedAt = timeProvider.now(),
                )
            }
        }
    }

    suspend fun addComment(postId: String, message: String, parentCommentId: String? = null) {
        val comment = Comment(
            id = idGenerator.newId(prefix = "comment"),
            postId = postId,
            parentCommentId = parentCommentId,
            author = currentUser,
            message = message,
            createdAt = timeProvider.now(),
            replyCount = 0,
            reactions = ReactionSummary(likeCount = 0, dislikeCount = 0, emojiCounts = emptyMap()),
            moderationState = CommentModerationState.VISIBLE,
        )
        _comments.update { comments ->
            val updated = comments[postId].orEmpty().toMutableList().apply { add(0, comment) }
            comments + (postId to updated)
        }
        _posts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    post.copy(stats = post.stats.copy(commentsCount = post.stats.commentsCount + 1))
                } else {
                    post
                }
            }
        }
        pushActivity(
            title = "Comment posted",
            body = "Your comment is now visible in the discussion.",
            kind = ActivityNotificationKind.COMMENT,
        )
    }

    suspend fun reactToComment(commentId: String, kind: ReactionKind, emoji: String?) {
        _comments.update { commentsByPost ->
            commentsByPost.mapValues { (_, comments) ->
                comments.map { comment ->
                    if (comment.id == commentId) {
                        val next = when (kind) {
                            ReactionKind.LIKE -> ViewerReaction.Like
                            ReactionKind.DISLIKE -> ViewerReaction.Dislike
                            ReactionKind.EMOJI -> ViewerReaction.Emoji(emoji.orEmpty())
                        }
                        comment.copy(reactions = updateReactionSummary(comment.reactions, next))
                    } else {
                        comment
                    }
                }
            }
        }
    }

    suspend fun sendMessage(chatId: String, text: String) {
        appendMessage(
            chatId = chatId,
            message = Message(
                id = idGenerator.newId(prefix = "msg"),
                chatId = chatId,
                sender = currentUser,
                sentAt = timeProvider.now(),
                kind = MessageKind.TEXT,
                text = text,
                attachments = emptyList(),
                isOutgoing = true,
            ),
        )
    }

    suspend fun createChat(request: CreateChatRequest): String {
        val chatId = idGenerator.newId(prefix = "chat")
        val createdAt = timeProvider.now()
        val chat = Chat(
            id = chatId,
            title = request.title.trim(),
            description = request.description.trim(),
            avatarUrl = request.avatarUri,
            visibility = request.visibility,
            members = listOf(currentUser),
            unreadCount = 0,
            isMuted = false,
            lastMessagePreview = request.description.trim().ifBlank { "Chat created" },
            lastActivityAt = createdAt,
        )
        _chats.update { listOf(chat) + it }
        _messages.update { current ->
            current + (
                chatId to listOf(
                    Message(
                        id = idGenerator.newId(prefix = "msg"),
                        chatId = chatId,
                        sender = currentUser,
                        sentAt = createdAt,
                        kind = MessageKind.SYSTEM,
                        text = if (request.visibility == ConversationVisibility.PUBLIC) {
                            "Public chat created"
                        } else {
                            "Private chat created"
                        },
                        attachments = emptyList(),
                        isOutgoing = true,
                        deliveryStatus = MessageDeliveryStatus.DELIVERED,
                        readStatus = MessageReadStatus.READ,
                    ),
                )
            )
        }
        pushActivity(
            title = "Chat created",
            body = "The chat ${request.title.trim()} is ready.",
            kind = ActivityNotificationKind.SYSTEM,
        )
        return chatId
    }

    suspend fun createChannel(request: CreateChannelRequest): String {
        val channelId = idGenerator.newId(prefix = "channel")
        val isPublic = request.visibility == ConversationVisibility.PUBLIC
        val requestedHandle = request.handle?.trim()?.removePrefix("@")
        val handle = when {
            isPublic && !requestedHandle.isNullOrBlank() -> requestedHandle
            isPublic -> "channel_${channelId.takeLast(6)}"
            else -> "private_${channelId.takeLast(6)}"
        }
        val channel = Channel(
            id = channelId,
            title = request.title.trim(),
            handle = "@$handle",
            avatarUrl = request.avatarUri ?: "https://picsum.photos/seed/$channelId/200/200",
            description = request.description.trim(),
            topics = setOf("dotchat"),
            followerCount = 1,
            isSubscribed = true,
            isVerified = false,
            coverUrl = "https://picsum.photos/seed/${channelId}cover/960/540",
            visibility = request.visibility,
            inviteLink = if (isPublic) null else "https://dotchat.app/invite/$channelId",
        )
        _channels.update { listOf(channel) + it }
        pushActivity(
            title = "Channel created",
            body = "The channel ${request.title.trim()} is now available.",
            kind = ActivityNotificationKind.FOLLOW,
        )
        return channelId
    }

    suspend fun forwardPost(postId: String, chatId: String, note: String?) {
        appendMessage(
            chatId = chatId,
            message = Message(
                id = idGenerator.newId(prefix = "msg"),
                chatId = chatId,
                sender = currentUser,
                sentAt = timeProvider.now(),
                kind = MessageKind.FORWARDED_POST,
                text = note.orEmpty(),
                attachments = emptyList<MediaAttachment>(),
                forwardedPostId = postId,
                isOutgoing = true,
            ),
        )
        pushActivity(
            title = "Post sent",
            body = "The feed post was forwarded into a chat.",
            kind = ActivityNotificationKind.FORWARD,
        )
    }

    private fun appendMessage(chatId: String, message: Message) {
        _messages.update { current ->
            current + (chatId to (current[chatId].orEmpty() + message))
        }
        _chats.update { chats ->
            chats.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(
                        lastMessagePreview = if (message.kind == MessageKind.FORWARDED_POST) "Forwarded post" else message.text,
                        lastActivityAt = message.sentAt,
                    )
                } else {
                    chat
                }
            }
        }
    }

    private fun pushActivity(
        title: String,
        body: String,
        kind: ActivityNotificationKind,
    ) {
        _activity.update { current ->
            listOf(
                ActivityNotification(
                    id = idGenerator.newId(prefix = "activity"),
                    title = title,
                    body = body,
                    createdAt = timeProvider.now(),
                    kind = kind,
                    actorAvatarUrl = currentUser.avatarUrl,
                ),
            ) + current
        }
    }

    private fun updateReactionSummary(
        summary: ReactionSummary,
        nextViewerReaction: ViewerReaction,
    ): ReactionSummary {
        var likes = summary.likeCount
        var dislikes = summary.dislikeCount
        val emojis = summary.emojiCounts.toMutableMap()

        when (val current = summary.viewerReaction) {
            ViewerReaction.Like -> likes -= 1
            ViewerReaction.Dislike -> dislikes -= 1
            is ViewerReaction.Emoji -> emojis[current.value] = (emojis[current.value] ?: 1) - 1
            ViewerReaction.None -> Unit
        }

        when (nextViewerReaction) {
            ViewerReaction.Like -> likes += 1
            ViewerReaction.Dislike -> dislikes += 1
            is ViewerReaction.Emoji -> emojis[nextViewerReaction.value] = (emojis[nextViewerReaction.value] ?: 0) + 1
            ViewerReaction.None -> Unit
        }

        return summary.copy(
            likeCount = likes.coerceAtLeast(0),
            dislikeCount = dislikes.coerceAtLeast(0),
            emojiCounts = emojis.filterValues { it > 0 },
            viewerReaction = nextViewerReaction,
        )
    }

    private fun Double.absoluteValue(): Double = if (this < 0) -this else this
}
