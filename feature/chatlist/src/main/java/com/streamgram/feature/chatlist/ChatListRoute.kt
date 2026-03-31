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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamSearchBar
import com.streamgram.core.designsystem.component.StreamUnreadBadge
import com.streamgram.core.designsystem.theme.StreamGramTheme
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.Chat
import com.streamgram.core.model.User
import com.streamgram.core.ui.StreamAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatListRoute(
    onOpenChat: (String) -> Unit,
    onCreateChat: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    ChatListScreen(
        chats = state.chats,
        onOpenChat = onOpenChat,
        onCreateChat = onCreateChat,
    )
}

@Composable
private fun ChatListScreen(
    chats: List<Chat>,
    onOpenChat: (String) -> Unit,
    onCreateChat: () -> Unit,
) {
    val query = remember { mutableStateOf("") }
    val filteredChats = remember(chats, query.value) {
        if (query.value.isBlank()) chats else chats.filter {
            it.title.contains(query.value, ignoreCase = true) ||
                it.lastMessagePreview.contains(query.value, ignoreCase = true)
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item {
                StreamSearchBar(
                    value = query.value,
                    onValueChange = { query.value = it },
                    placeholder = stringResource(R.string.chat_search_placeholder),
                )
            }
            if (filteredChats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.chat_list_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.chat_list_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(filteredChats, key = Chat::id) { chat ->
                ChatRow(
                    chat = chat,
                    onClick = { onOpenChat(chat.id) },
                )
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

@Preview
@Composable
private fun ChatListScreenPreview() {
    StreamGramTheme(darkTheme = false) {
        ChatListScreen(
            chats = listOf(
                Chat(
                    id = "chat-1",
                    title = "Alice Nova",
                    avatarUrl = null,
                    members = listOf(
                        User("1", "alice", "Alice Nova", "", "", 0, 0, 0),
                    ),
                    unreadCount = 3,
                    isMuted = false,
                    lastMessagePreview = "Send me the runtime config post",
                    lastActivityAt = Instant.now(),
                ),
            ),
            onOpenChat = {},
            onCreateChat = {},
        )
    }
}
