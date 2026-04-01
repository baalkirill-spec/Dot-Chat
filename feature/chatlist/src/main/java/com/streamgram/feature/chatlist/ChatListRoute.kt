package com.streamgram.feature.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamSearchBar
import com.streamgram.core.designsystem.component.StreamUnreadBadge
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.Channel
import com.streamgram.core.model.Chat
import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.ui.StreamAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun ChatListRoute(
    onOpenChat: (String) -> Unit,
    onOpenChannel: ((String) -> Unit)? = null,
    onCreateChat: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val filteredChats = remember(state.chats, state.query) {
        if (state.query.isBlank()) state.chats else state.chats.filter {
            it.title.contains(state.query, ignoreCase = true) ||
                it.lastMessagePreview.contains(state.query, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = StreamTheme.spacing.lg,
                top = StreamTheme.spacing.md,
                end = StreamTheme.spacing.lg,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        ) {
            item {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item {
                StreamSearchBar(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    placeholder = stringResource(R.string.chat_search_placeholder),
                )
            }

            // ── Chats section ────────────────────────────────────────────
            if (state.query.isNotBlank() && filteredChats.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.nav_chats))
                }
            }
            if (filteredChats.isEmpty() && state.query.isBlank()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.chat_list_empty_title),
                        body = stringResource(R.string.chat_list_empty_body),
                    )
                }
            }
            items(filteredChats, key = Chat::id) { chat ->
                ChatRow(
                    chat = chat,
                    onClick = { onOpenChat(chat.id) },
                )
            }

            // ── Loading indicator for remote search ──────────────────────
            if (state.isSearching) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
            }

            // ── People section ───────────────────────────────────────────
            if (state.query.isNotBlank() && state.peopleResults.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.search_section_people))
                }
                items(state.peopleResults, key = { it.user.id }) { result ->
                    PersonResultRow(
                        result = result,
                        onClick = {
                            scope.launch {
                                val chatId = viewModel.startChatWith(result.user.id)
                                onOpenChat(chatId)
                            }
                        },
                    )
                }
            }

            // ── Channels section ─────────────────────────────────────────
            if (state.query.isNotBlank() && state.channelResults.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.search_section_channels))
                }
                items(state.channelResults, key = Channel::id) { channel ->
                    ChannelResultRow(
                        channel = channel,
                        onClick = { onOpenChannel?.invoke(channel.id) },
                    )
                }
            }

            // ── No results ───────────────────────────────────────────────
            if (state.query.isNotBlank() && !state.isSearching &&
                filteredChats.isEmpty() && state.peopleResults.isEmpty() && state.channelResults.isEmpty()
            ) {
                item {
                    EmptyState(
                        title = stringResource(R.string.search_no_results_title),
                        body = stringResource(R.string.search_no_results_body),
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateChat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = StreamTheme.spacing.lg, bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
    )
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PersonResultRow(
    result: ContactSearchResult,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreamAvatar(
            imageUrl = result.user.avatarUrl,
            fallbackLabel = result.user.displayName,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.user.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "@${result.user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChannelResultRow(
    channel: Channel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreamAvatar(
            imageUrl = channel.avatarUrl,
            fallbackLabel = channel.title,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = channel.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ChatRow(
    chat: Chat,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        StreamAvatar(
            imageUrl = chat.avatarUrl,
            fallbackLabel = chat.title,
            size = 56.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatChatTime(chat.lastActivityAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.lastMessagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                if (chat.isMuted) {
                    Text(
                        text = stringResource(R.string.chat_muted),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                if (chat.unreadCount > 0) {
                    StreamUnreadBadge(value = chat.unreadCount.toString())
                }
            }
        }
    }
}

private fun formatChatTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}
