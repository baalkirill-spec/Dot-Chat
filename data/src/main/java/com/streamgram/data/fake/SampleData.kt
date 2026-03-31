package com.streamgram.data.fake

import com.streamgram.core.model.ActivityNotification
import com.streamgram.core.model.ActivityNotificationKind
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentModerationState
import com.streamgram.core.model.Friend
import com.streamgram.core.model.MediaAttachment
import com.streamgram.core.model.MediaAttachmentKind
import com.streamgram.core.model.MediaPost
import com.streamgram.core.model.MediaPostKind
import com.streamgram.core.model.Message
import com.streamgram.core.model.MessageKind
import com.streamgram.core.model.PostStats
import com.streamgram.core.model.PostViewerState
import com.streamgram.core.model.ReactionSummary
import com.streamgram.core.model.RecommendationProfile
import com.streamgram.core.model.User
import com.streamgram.core.model.ViewerReaction
import java.time.Instant

data class SampleState(
    val currentUser: User,
    val friends: List<Friend>,
    val channels: List<Channel>,
    val posts: List<MediaPost>,
    val comments: Map<String, List<Comment>>,
    val chats: List<Chat>,
    val messages: Map<String, List<Message>>,
    val activity: List<ActivityNotification>,
    val recommendationProfile: RecommendationProfile,
)

object SampleData {
    fun initial(now: Instant): SampleState {
        val currentUser = User(
            id = "user-me",
            handle = "kirill",
            displayName = "Kirill",
            avatarUrl = "https://picsum.photos/id/1005/200/200",
            bio = "Product-minded builder. Watches the full cut, forwards the best takes, and keeps chat quality high.",
            subscriptionCount = 3,
            chatCount = 4,
            savedPostCount = 2,
        )
        val alice = User(
            id = "user-alice",
            handle = "alice",
            displayName = "Alice Nova",
            avatarUrl = "https://picsum.photos/id/1012/200/200",
            bio = "Design systems, motion, and iOS-quality details.",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
        )
        val bob = User(
            id = "user-bob",
            handle = "bob",
            displayName = "Bob Vega",
            avatarUrl = "https://picsum.photos/id/1027/200/200",
            bio = "Travel storyteller.",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
        )
        val maya = User(
            id = "user-maya",
            handle = "maya",
            displayName = "Maya Flux",
            avatarUrl = "https://picsum.photos/id/1001/200/200",
            bio = "Android and AI engineer.",
            subscriptionCount = 0,
            chatCount = 0,
            savedPostCount = 0,
        )

        val friends = listOf(
            Friend(id = alice.id, displayName = alice.displayName, avatarUrl = alice.avatarUrl, lastSeenAt = now.minusSeconds(60)),
            Friend(id = bob.id, displayName = bob.displayName, avatarUrl = bob.avatarUrl, lastSeenAt = now.minusSeconds(900)),
            Friend(id = maya.id, displayName = maya.displayName, avatarUrl = maya.avatarUrl, lastSeenAt = now.minusSeconds(300)),
        )

        val channels = listOf(
            Channel(
                id = "channel-ux",
                title = "UX Signal",
                handle = "@uxsignal",
                avatarUrl = "https://picsum.photos/id/1040/200/200",
                description = "Motion, typography, and product polish for premium mobile clients.",
                topics = setOf("design", "mobile", "motion"),
                followerCount = 182_000,
                isSubscribed = true,
                isVerified = true,
                coverUrl = "https://picsum.photos/id/1043/1200/600",
            ),
            Channel(
                id = "channel-labs",
                title = "Build Labs",
                handle = "@buildlabs",
                avatarUrl = "https://picsum.photos/id/1057/200/200",
                description = "Android architecture, AI explainers, and high-signal build notes.",
                topics = setOf("android", "ai", "architecture"),
                followerCount = 241_300,
                isSubscribed = true,
                isVerified = true,
                coverUrl = "https://picsum.photos/id/1059/1200/600",
            ),
            Channel(
                id = "channel-nomad",
                title = "Nomad Frame",
                handle = "@nomadframe",
                avatarUrl = "https://picsum.photos/id/1062/200/200",
                description = "Cinematic travel cuts and atmospheric photo stories.",
                topics = setOf("travel", "photo", "video"),
                followerCount = 127_800,
                isSubscribed = false,
                isVerified = false,
                coverUrl = "https://picsum.photos/id/1069/1200/600",
            ),
            Channel(
                id = "channel-science",
                title = "Pocket Science",
                handle = "@pocketscience",
                avatarUrl = "https://picsum.photos/id/1074/200/200",
                description = "Readable science explainers with clean visual storytelling.",
                topics = setOf("science", "education", "video"),
                followerCount = 96_450,
                isSubscribed = true,
                isVerified = false,
                coverUrl = "https://picsum.photos/id/1084/1200/600",
            ),
        )

        val posts = listOf(
            MediaPost(
                id = "post-1",
                channelId = "channel-labs",
                kind = MediaPostKind.SHORT_VIDEO,
                attachments = listOf(
                    MediaAttachment(
                        id = "att-1",
                        kind = MediaAttachmentKind.VIDEO,
                        url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                        previewUrl = "https://picsum.photos/id/1035/1080/1920",
                        aspectRatio = 9f / 16f,
                        durationSeconds = 24,
                    ),
                ),
                caption = "Why feature flags and runtime config deserve their own layer even in the first release.",
                hashtags = listOf("#android", "#architecture", "#featureflags"),
                publishedAt = now.minusSeconds(3_600),
                stats = PostStats(
                    commentsCount = 182,
                    sharesCount = 48,
                    savesCount = 312,
                    reactionSummary = ReactionSummary(
                        likeCount = 8_920,
                        dislikeCount = 164,
                        emojiCounts = mapOf("fire" to 602, "brain" to 411),
                    ),
                ),
                viewerState = PostViewerState(watchCompletion = 0.86f, watchedSeconds = 21),
            ),
            MediaPost(
                id = "post-2",
                channelId = "channel-ux",
                kind = MediaPostKind.PHOTO,
                attachments = listOf(
                    MediaAttachment(
                        id = "att-2",
                        kind = MediaAttachmentKind.PHOTO,
                        url = "https://picsum.photos/id/1037/1080/1920",
                        previewUrl = "https://picsum.photos/id/1037/540/960",
                        aspectRatio = 9f / 16f,
                    ),
                ),
                caption = "A refined motion pattern for a large CTA inside a full-screen feed.",
                hashtags = listOf("#design", "#motion", "#compose"),
                publishedAt = now.minusSeconds(7_200),
                stats = PostStats(
                    commentsCount = 74,
                    sharesCount = 23,
                    savesCount = 209,
                    reactionSummary = ReactionSummary(
                        likeCount = 5_102,
                        dislikeCount = 92,
                        emojiCounts = mapOf("sparkles" to 355, "idea" to 191),
                    ),
                ),
                viewerState = PostViewerState(watchCompletion = 0.42f, watchedSeconds = 5),
            ),
            MediaPost(
                id = "post-3",
                channelId = "channel-nomad",
                kind = MediaPostKind.SHORT_VIDEO,
                attachments = listOf(
                    MediaAttachment(
                        id = "att-3",
                        kind = MediaAttachmentKind.VIDEO,
                        url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                        previewUrl = "https://picsum.photos/id/1045/1080/1920",
                        aspectRatio = 9f / 16f,
                        durationSeconds = 18,
                    ),
                ),
                caption = "Northern fog, one lens, and the kind of ambient sound that makes you watch to the end.",
                hashtags = listOf("#travel", "#cinematic", "#shorts"),
                publishedAt = now.minusSeconds(10_800),
                stats = PostStats(
                    commentsCount = 41,
                    sharesCount = 33,
                    savesCount = 118,
                    reactionSummary = ReactionSummary(
                        likeCount = 3_302,
                        dislikeCount = 54,
                        emojiCounts = mapOf("fog" to 411, "heart" to 209),
                    ),
                ),
                viewerState = PostViewerState(watchCompletion = 0.12f, watchedSeconds = 2),
            ),
            MediaPost(
                id = "post-4",
                channelId = "channel-science",
                kind = MediaPostKind.CARD,
                attachments = listOf(
                    MediaAttachment(
                        id = "att-4",
                        kind = MediaAttachmentKind.PHOTO,
                        url = "https://picsum.photos/id/1050/1080/1920",
                        previewUrl = "https://picsum.photos/id/1050/540/960",
                        aspectRatio = 9f / 16f,
                    ),
                ),
                caption = "A quick explainer on why a space image feels sharper after careful downsampling.",
                hashtags = listOf("#science", "#space", "#visuals"),
                publishedAt = now.minusSeconds(1_800),
                stats = PostStats(
                    commentsCount = 29,
                    sharesCount = 17,
                    savesCount = 144,
                    reactionSummary = ReactionSummary(
                        likeCount = 2_810,
                        dislikeCount = 39,
                        emojiCounts = mapOf("rocket" to 318, "satellite" to 208),
                    ),
                ),
                viewerState = PostViewerState(watchCompletion = 0.97f, watchedSeconds = 28, isSaved = true),
            ),
            MediaPost(
                id = "post-5",
                channelId = "channel-labs",
                kind = MediaPostKind.LONG_VIDEO,
                attachments = listOf(
                    MediaAttachment(
                        id = "att-5",
                        kind = MediaAttachmentKind.VIDEO,
                        url = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                        previewUrl = "https://picsum.photos/id/1068/1080/1920",
                        aspectRatio = 9f / 16f,
                        durationSeconds = 74,
                    ),
                ),
                caption = "Breaking down feed ranking with cold start, watch depth, follows, and topic affinity.",
                hashtags = listOf("#ranking", "#ai", "#product"),
                publishedAt = now.minusSeconds(5_400),
                stats = PostStats(
                    commentsCount = 213,
                    sharesCount = 61,
                    savesCount = 411,
                    reactionSummary = ReactionSummary(
                        likeCount = 10_120,
                        dislikeCount = 133,
                        emojiCounts = mapOf("lab" to 480, "chart" to 266),
                        viewerReaction = ViewerReaction.Like,
                    ),
                ),
                viewerState = PostViewerState(watchCompletion = 0.91f, watchedSeconds = 66, hasImpression = true),
            ),
        )

        val comments = mapOf(
            "post-1" to listOf(
                Comment(
                    id = "comment-1",
                    postId = "post-1",
                    parentCommentId = null,
                    author = alice,
                    message = "A dedicated runtime config module pays for itself the moment A/B tests begin.",
                    createdAt = now.minusSeconds(1_400),
                    replyCount = 2,
                    reactions = ReactionSummary(likeCount = 42, dislikeCount = 0, emojiCounts = mapOf("fire" to 12)),
                    moderationState = CommentModerationState.VISIBLE,
                ),
                Comment(
                    id = "comment-2",
                    postId = "post-1",
                    parentCommentId = null,
                    author = maya,
                    message = "Would love to see the fallback policy for offline-first comments next.",
                    createdAt = now.minusSeconds(900),
                    replyCount = 0,
                    reactions = ReactionSummary(likeCount = 15, dislikeCount = 0, emojiCounts = mapOf("idea" to 4)),
                    moderationState = CommentModerationState.VISIBLE,
                ),
            ),
            "post-4" to listOf(
                Comment(
                    id = "comment-3",
                    postId = "post-4",
                    parentCommentId = null,
                    author = bob,
                    message = "These editorial cards break up the feed nicely between heavier videos.",
                    createdAt = now.minusSeconds(400),
                    replyCount = 1,
                    reactions = ReactionSummary(likeCount = 9, dislikeCount = 0, emojiCounts = mapOf("rocket" to 3)),
                    moderationState = CommentModerationState.VISIBLE,
                ),
            ),
        )

        val chats = listOf(
            Chat(
                id = "chat-1",
                title = "Alice Nova",
                avatarUrl = alice.avatarUrl,
                members = listOf(currentUser, alice),
                unreadCount = 2,
                isMuted = false,
                lastMessagePreview = "Send me the runtime config post",
                lastActivityAt = now.minusSeconds(120),
            ),
            Chat(
                id = "chat-2",
                title = "MVP Crew",
                avatarUrl = null,
                members = listOf(currentUser, alice, maya),
                unreadCount = 0,
                isMuted = true,
                lastMessagePreview = "Let's finish the feed shell today",
                lastActivityAt = now.minusSeconds(1_800),
            ),
        )

        val messages = mapOf(
            "chat-1" to listOf(
                Message(
                    id = "msg-1",
                    chatId = "chat-1",
                    sender = alice,
                    sentAt = now.minusSeconds(300),
                    kind = MessageKind.TEXT,
                    text = "Send me the runtime config post",
                    attachments = emptyList(),
                    isOutgoing = false,
                ),
                Message(
                    id = "msg-2",
                    chatId = "chat-1",
                    sender = currentUser,
                    sentAt = now.minusSeconds(120),
                    kind = MessageKind.FORWARDED_POST,
                    text = "This is the breakdown I meant.",
                    attachments = emptyList(),
                    forwardedPostId = "post-1",
                    isOutgoing = true,
                ),
            ),
            "chat-2" to listOf(
                Message(
                    id = "msg-3",
                    chatId = "chat-2",
                    sender = maya,
                    sentAt = now.minusSeconds(2_000),
                    kind = MessageKind.TEXT,
                    text = "Let's finish the feed shell today",
                    attachments = emptyList(),
                    isOutgoing = false,
                ),
            ),
        )

        val activity = listOf(
            ActivityNotification(
                id = "activity-1",
                title = "New comment",
                body = "Alice Nova replied under the feature flags post.",
                createdAt = now.minusSeconds(600),
                kind = ActivityNotificationKind.COMMENT,
                actorAvatarUrl = alice.avatarUrl,
            ),
            ActivityNotification(
                id = "activity-2",
                title = "Channel growth",
                body = "14 new people followed @buildlabs after the latest video.",
                createdAt = now.minusSeconds(2_000),
                kind = ActivityNotificationKind.FOLLOW,
                actorAvatarUrl = null,
            ),
        )

        val recommendationProfile = RecommendationProfile(
            userId = currentUser.id,
            interests = mapOf("android" to 1.0, "architecture" to 0.92, "design" to 0.76, "science" to 0.58, "travel" to 0.18),
            negativeSignals = mapOf("travel" to 0.24),
            followedChannelIds = channels.filter(Channel::isSubscribed).map(Channel::id).toSet(),
            experimentBucket = "mvp_control",
            lastUpdatedAt = now,
        )

        return SampleState(currentUser, friends, channels, posts, comments, chats, messages, activity, recommendationProfile)
    }
}
