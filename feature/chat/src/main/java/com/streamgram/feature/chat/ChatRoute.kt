@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.streamgram.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.CircularIconPlate
import com.streamgram.core.designsystem.component.StreamGlassCard
import com.streamgram.core.designsystem.component.StreamMessageBubble
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamTextField
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.CallType
import com.streamgram.core.model.Chat
import com.streamgram.core.model.Message
import com.streamgram.core.model.MessageKind
import com.streamgram.core.ui.StreamAvatar
import kotlinx.coroutines.launch

@Composable
fun ChatRoute(
    onBack: () -> Unit,
    onOpenCall: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val callStartFailed = stringResource(R.string.call_start_failed)

    ChatScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSendMessage = viewModel::sendMessage,
        onStartAudioCall = {
            scope.launch {
                if (viewModel.startCall(CallType.AUDIO)) {
                    onOpenCall()
                } else {
                    snackbarHostState.showSnackbar(callStartFailed)
                }
            }
        },
        onStartVideoCall = {
            scope.launch {
                if (viewModel.startCall(CallType.VIDEO)) {
                    onOpenCall()
                } else {
                    snackbarHostState.showSnackbar(callStartFailed)
                }
            }
        },
    )
}

@Composable
private fun ChatScreen(
    state: ChatUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StreamAvatar(
                            imageUrl = state.chat?.avatarUrl,
                            fallbackLabel = state.chat?.title ?: stringResource(R.string.chat_title_fallback),
                        )
                        Column {
                            Text(
                                text = state.chat?.title ?: stringResource(R.string.chat_title_fallback),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.chat_online),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.callTargetUserId != null) {
                        IconButton(onClick = onStartAudioCall) {
                            Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.chat_call))
                        }
                        TextButton(onClick = onStartVideoCall) {
                            Text(text = stringResource(R.string.chat_video_call))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            StreamGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = StreamTheme.spacing.md, vertical = StreamTheme.spacing.sm),
                contentPadding = PaddingValues(StreamTheme.spacing.sm),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StreamTextField(
                        value = state.draft,
                        onValueChange = onDraftChanged,
                        label = stringResource(R.string.chat_input_placeholder),
                        modifier = Modifier.weight(1f),
                    )
                    CircularIconPlate(
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = onSendMessage,
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = StreamTheme.spacing.lg,
                    vertical = StreamTheme.spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
            ) {
                items(state.messages, key = Message::id) { message ->
                    MessageBubbleRow(message = message)
                }
            }
        }
    }
}

@Composable
private fun MessageBubbleRow(
    message: Message,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start,
    ) {
        StreamMessageBubble(
            text = if (message.kind == MessageKind.FORWARDED_POST) {
                stringResource(R.string.chat_forwarded_post)
            } else {
                message.text
            },
            supportingText = if (message.kind == MessageKind.FORWARDED_POST && message.text.isNotBlank()) {
                message.text
            } else {
                null
            },
            isOutgoing = message.isOutgoing,
        )
    }
}

@Composable
fun ForwardPostRoute(
    onBack: () -> Unit,
    onForwarded: () -> Unit,
    viewModel: ForwardPostViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.forwardedChatId) {
        if (state.forwardedChatId != null) {
            onForwarded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.forward_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(StreamTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
            ) {
                items(state.chats, key = Chat::id) { chat ->
                    StreamGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(StreamTheme.spacing.md),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StreamAvatar(chat.avatarUrl, chat.title)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(chat.lastMessagePreview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StreamPrimaryButton(
                                text = stringResource(R.string.forward_action),
                                onClick = { viewModel.forwardToChat(chat.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
