package com.streamgram.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.streamgram.core.designsystem.component.CircularIconPlate
import com.streamgram.core.designsystem.theme.BlackAlpha44
import com.streamgram.core.designsystem.theme.StreamGramTheme
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.Channel
import com.streamgram.core.model.FeedItem
import com.streamgram.core.model.FeedOrigin
import com.streamgram.core.model.MediaAttachment
import com.streamgram.core.model.MediaAttachmentKind
import com.streamgram.core.model.MediaPost
import com.streamgram.core.model.MediaPostKind
import com.streamgram.core.model.PostStats
import com.streamgram.core.model.PostViewerState
import com.streamgram.core.model.ReactionSummary
import com.streamgram.core.model.RecommendationScoreBreakdown
import com.streamgram.core.model.WatchEventType
import com.streamgram.core.player.StreamVideoPlayer
import com.streamgram.core.ui.EmptyStatePane
import com.streamgram.core.ui.StreamAvatar
import java.time.Instant
import kotlinx.coroutines.delay

@Composable
fun FeedRoute(
    onOpenComments: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onForwardPost: (String) -> Unit,
    onSharePost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    FeedScreen(
        state = state,
        modifier = modifier,
        onOpenComments = onOpenComments,
        onOpenChannel = onOpenChannel,
        onForwardPost = onForwardPost,
        onSharePost = onSharePost,
        onToggleLike = viewModel::onToggleLike,
        onToggleDislike = viewModel::onToggleDislike,
        onToggleSave = viewModel::onToggleSave,
        onToggleFollow = viewModel::onToggleFollow,
        onWatchMilestone = viewModel::onWatchMilestone,
    )
}

@Composable
private fun FeedScreen(
    state: FeedUiState,
    onOpenComments: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onForwardPost: (String) -> Unit,
    onSharePost: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleDislike: (String) -> Unit,
    onToggleSave: (String, Boolean) -> Unit,
    onToggleFollow: (String, Boolean) -> Unit,
    onWatchMilestone: (String, String, WatchEventType, Float, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedTab = remember { mutableIntStateOf(if (state.recommendedFeedEnabled) 2 else 0) }
    val displayedItems = remember(state.items, selectedTab.intValue, state.recommendedFeedEnabled) {
        when (selectedTab.intValue) {
            1 -> state.items.filter { it.channel.isSubscribed }
            2 -> if (state.recommendedFeedEnabled) state.items else state.items.filter { it.channel.isSubscribed }
            else -> state.items
        }
    }

    if (!state.isLoading && displayedItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStatePane(
                title = stringResource(R.string.feed_empty_title),
                subtitle = stringResource(R.string.feed_empty_body),
                modifier = Modifier.padding(24.dp),
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { displayedItems.size })
    LaunchedEffect(selectedTab.intValue, displayedItems.size) {
        if (displayedItems.isNotEmpty() && pagerState.currentPage > displayedItems.lastIndex) {
            pagerState.scrollToPage(0)
        }
    }

    val activeItem = displayedItems.getOrNull(pagerState.currentPage)

    LaunchedEffect(activeItem?.id) {
        val item = activeItem ?: return@LaunchedEffect
        val attachment = item.post.attachments.firstOrNull()
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.IMPRESSION, 0f, 0)
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.VIEW_START, 0f, 0)
        delay(2_000)
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.WATCHED_2S, 0.12f, 2)
        val duration = attachment?.durationSeconds ?: return@LaunchedEffect
        delay(((duration * 500L) - 2_000L).coerceAtLeast(250L))
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.WATCHED_50_PERCENT, 0.5f, duration / 2)
        delay((duration * 450L).coerceAtLeast(500L))
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.WATCHED_95_PERCENT, 0.95f, (duration * 0.95f).toInt())
        delay((duration * 50L).coerceAtLeast(250L))
        onWatchMilestone(item.post.id, item.channel.id, WatchEventType.COMPLETED, 1f, duration)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        VerticalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { page ->
            val item = displayedItems[page]
            FeedPage(
                item = item,
                autoplayEnabled = state.autoplayEnabled,
                followsEnabled = state.followsEnabled,
                isActive = page == pagerState.currentPage,
                onOpenComments = onOpenComments,
                onOpenChannel = onOpenChannel,
                onForwardPost = onForwardPost,
                onSharePost = onSharePost,
                onToggleLike = onToggleLike,
                onToggleDislike = onToggleDislike,
                onToggleSave = onToggleSave,
                onToggleFollow = onToggleFollow,
            )
        }

        FeedTopBar(
            selectedIndex = selectedTab.intValue,
            onTabSelected = { selectedTab.intValue = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.md),
        )
    }
}

@Composable
private fun FeedTopBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        stringResource(R.string.feed_tab_watch),
        stringResource(R.string.feed_tab_following),
        stringResource(R.string.feed_tab_recommendations),
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg)) {
            tabs.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) },
                ) {
                    Text(
                        text = title,
                        color = if (selectedIndex == index) Color.White else Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(width = 28.dp, height = 2.dp)
                            .background(if (selectedIndex == index) Color.White else Color.Transparent),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun FeedPage(
    item: FeedItem,
    autoplayEnabled: Boolean,
    followsEnabled: Boolean,
    isActive: Boolean,
    onOpenComments: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onForwardPost: (String) -> Unit,
    onSharePost: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleDislike: (String) -> Unit,
    onToggleSave: (String, Boolean) -> Unit,
    onToggleFollow: (String, Boolean) -> Unit,
) {
    val attachment = remember(item.post.id) { item.post.attachments.first() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
                )
            },
    ) {
        when (attachment.kind) {
            MediaAttachmentKind.VIDEO -> StreamVideoPlayer(
                url = attachment.url,
                shouldPlay = autoplayEnabled && isActive,
                modifier = Modifier.fillMaxSize(),
            )

            MediaAttachmentKind.PHOTO -> AsyncImage(
                model = attachment.url,
                contentDescription = item.post.caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        FeedActionRail(
            item = item,
            followsEnabled = followsEnabled,
            onOpenComments = onOpenComments,
            onOpenChannel = onOpenChannel,
            onForwardPost = onForwardPost,
            onSharePost = onSharePost,
            onToggleLike = onToggleLike,
            onToggleDislike = onToggleDislike,
            onToggleSave = onToggleSave,
            onToggleFollow = onToggleFollow,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .navigationBarsPadding()
                .padding(end = StreamTheme.spacing.md, bottom = 108.dp),
        )

        FeedBottomInfo(
            item = item,
            onOpenChannel = onOpenChannel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.xl),
        )
    }
}

@Composable
private fun FeedActionRail(
    item: FeedItem,
    followsEnabled: Boolean,
    onOpenComments: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onForwardPost: (String) -> Unit,
    onSharePost: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleDislike: (String) -> Unit,
    onToggleSave: (String, Boolean) -> Unit,
    onToggleFollow: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            StreamAvatar(
                imageUrl = item.channel.avatarUrl,
                fallbackLabel = item.channel.title,
                size = 56.dp,
                modifier = Modifier.clickable { onOpenChannel(item.channel.id) },
            )
            if (followsEnabled && !item.channel.isSubscribed) {
                Box(
                    modifier = Modifier
                        .padding(top = 44.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onToggleFollow(item.channel.id, item.channel.isSubscribed) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        RailAction(
            icon = Icons.Filled.Favorite,
            label = compactMetric(item.post.stats.reactionSummary.likeCount),
            onClick = { onToggleLike(item.post.id) },
        )
        RailAction(
            icon = Icons.Filled.Close,
            label = compactMetric(item.post.stats.reactionSummary.dislikeCount),
            onClick = { onToggleDislike(item.post.id) },
        )
        RailAction(
            icon = Icons.Filled.Info,
            label = compactMetric(item.post.stats.commentsCount),
            onClick = { onOpenComments(item.post.id) },
        )
        RailAction(
            icon = Icons.Filled.Share,
            label = compactMetric(item.post.stats.sharesCount),
            onClick = { onSharePost(item.post.id) },
        )
        RailAction(
            icon = Icons.Filled.Send,
            label = stringResource(R.string.feed_send),
            onClick = { onForwardPost(item.post.id) },
        )
        RailAction(
            icon = Icons.Filled.Star,
            label = if (item.post.viewerState.isSaved) stringResource(R.string.feed_saved) else stringResource(R.string.feed_save),
            onClick = { onToggleSave(item.post.id, !item.post.viewerState.isSaved) },
        )
    }
}

@Composable
private fun RailAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularIconPlate(
            icon = icon,
            onClick = onClick,
            tint = Color.White,
            backgroundColor = BlackAlpha44,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun FeedBottomInfo(
    item: FeedItem,
    onOpenChannel: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
    ) {
        Text(
            text = item.channel.title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onOpenChannel(item.channel.id) },
        )
        Text(
            text = item.channel.handle,
            color = Color.White.copy(alpha = 0.76f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = item.post.caption,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.post.hashtags.joinToString(" "),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(BlackAlpha44)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.feed_fullscreen),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

private fun compactMetric(value: Int): String = when {
    value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
    value >= 1_000 -> String.format("%.1fK", value / 1_000f)
    else -> value.toString()
}

@Preview
@Composable
private fun FeedPreview() {
    StreamGramTheme(darkTheme = false) {
        FeedScreen(
            state = FeedUiState(
                items = listOf(
                    FeedItem(
                        id = "feed-1",
                        post = MediaPost(
                            id = "post-1",
                            channelId = "channel-1",
                            kind = MediaPostKind.PHOTO,
                            attachments = listOf(
                                MediaAttachment(
                                    id = "att-1",
                                    kind = MediaAttachmentKind.PHOTO,
                                    url = "https://picsum.photos/id/1037/1080/1920",
                                    previewUrl = "https://picsum.photos/id/1037/540/960",
                                    aspectRatio = 9f / 16f,
                                ),
                            ),
                            caption = "A clean Telegram-like shell with a stronger media front door.",
                            hashtags = listOf("#telegram", "#feed"),
                            publishedAt = Instant.now(),
                            stats = PostStats(
                                commentsCount = 117,
                                sharesCount = 89,
                                savesCount = 121,
                                reactionSummary = ReactionSummary(likeCount = 10_300, dislikeCount = 216, emojiCounts = emptyMap()),
                            ),
                            viewerState = PostViewerState(),
                        ),
                        channel = Channel(
                            id = "channel-1",
                            title = "RIS",
                            handle = "@ris",
                            avatarUrl = "https://picsum.photos/id/1005/200/200",
                            description = "",
                            topics = setOf("design"),
                            followerCount = 120_000,
                            isSubscribed = false,
                            isVerified = true,
                            coverUrl = null,
                        ),
                        origin = FeedOrigin.RECOMMENDED_CHANNEL,
                        ranking = RecommendationScoreBreakdown(totalScore = 0.8, signals = emptyList()),
                    ),
                ),
                isLoading = false,
            ),
            onOpenComments = {},
            onOpenChannel = {},
            onForwardPost = {},
            onSharePost = {},
            onToggleLike = {},
            onToggleDislike = {},
            onToggleSave = { _, _ -> },
            onToggleFollow = { _, _ -> },
            onWatchMilestone = { _, _, _, _, _ -> },
        )
    }
}
