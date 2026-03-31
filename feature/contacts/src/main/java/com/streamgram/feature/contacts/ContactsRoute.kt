package com.streamgram.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.CircularIconPlate
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSearchBar
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSecondaryButton
import com.streamgram.core.designsystem.theme.StreamGramTheme
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.CallType
import com.streamgram.core.model.Contact
import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.model.User
import com.streamgram.core.ui.StreamAvatar
import java.time.Instant
import kotlinx.coroutines.launch

@Composable
fun ContactsRoute(
    onOpenChat: (String) -> Unit,
    onOpenCall: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val callStartFailed = stringResource(R.string.call_start_failed)

    ContactsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onQueryChanged = viewModel::onQueryChanged,
        onContactSelected = { userId ->
            scope.launch {
                onOpenChat(viewModel.startChatWith(userId))
            }
        },
        onStartAudioCall = { userId ->
            scope.launch {
                if (viewModel.startCallWith(userId, CallType.AUDIO)) {
                    onOpenCall()
                } else {
                    snackbarHostState.showSnackbar(callStartFailed)
                }
            }
        },
        onStartVideoCall = { userId ->
            scope.launch {
                if (viewModel.startCallWith(userId, CallType.VIDEO)) {
                    onOpenCall()
                } else {
                    snackbarHostState.showSnackbar(callStartFailed)
                }
            }
        },
        onAddContact = viewModel::addContact,
        onRemoveContact = viewModel::removeContact,
    )
}

@Composable
private fun ContactsScreen(
    state: ContactsUiState,
    snackbarHostState: SnackbarHostState,
    onQueryChanged: (String) -> Unit,
    onContactSelected: (String) -> Unit,
    onStartAudioCall: (String) -> Unit,
    onStartVideoCall: (String) -> Unit,
    onAddContact: (String) -> Unit,
    onRemoveContact: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SnackbarHost(hostState = snackbarHostState)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.contacts_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            StreamSearchBar(
                value = state.query,
                onValueChange = onQueryChanged,
                placeholder = stringResource(R.string.contacts_search_placeholder),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        ) {
            if (state.query.isNotBlank()) {
                item {
                    Text(
                        text = stringResource(R.string.contacts_search_results),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                when {
                    state.isSearching -> item {
                        SearchStateCard(text = stringResource(R.string.contacts_search_loading))
                    }

                    state.searchResults.isEmpty() -> item {
                        SearchStateCard(text = stringResource(R.string.contacts_search_empty))
                    }

                    else -> item {
                        StreamSectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            Column {
                                state.searchResults.forEachIndexed { index, result ->
                                    SearchResultRow(
                                        result = result,
                                        onMessage = { onContactSelected(result.user.id) },
                                        onAudioCall = { onStartAudioCall(result.user.id) },
                                        onVideoCall = { onStartVideoCall(result.user.id) },
                                        onToggleContact = {
                                            if (result.alreadyInContacts) {
                                                onRemoveContact(result.user.id)
                                            } else {
                                                onAddContact(result.user.id)
                                            }
                                        },
                                    )
                                    if (index != state.searchResults.lastIndex) {
                                        DividerInset()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.contacts_sorting_hint),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                )
            }
            item {
                StreamSectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    if (state.contacts.isEmpty()) {
                        SearchStateCard(text = stringResource(R.string.contacts_empty_state))
                    } else {
                        Column {
                            state.contacts.forEachIndexed { index, contact ->
                                ContactRow(
                                    contact = contact,
                                    onClick = { onContactSelected(contact.contactUserId) },
                                    onAudioCall = { onStartAudioCall(contact.contactUserId) },
                                    onVideoCall = { onStartVideoCall(contact.contactUserId) },
                                )
                                if (index != state.contacts.lastIndex) {
                                    DividerInset()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: ContactSearchResult,
    onMessage: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onToggleContact: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = StreamTheme.spacing.md, vertical = StreamTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreamAvatar(
            imageUrl = result.user.avatarUrl,
            fallbackLabel = result.user.displayName,
            size = 52.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = result.user.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (result.matchedByPhone) {
                    result.user.phoneNumber ?: "@${result.user.username}"
                } else {
                    "@${result.user.username}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
            StreamSecondaryButton(
                text = stringResource(R.string.contacts_message_action),
                onClick = onMessage,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
                CircularIconPlate(
                    icon = Icons.Filled.Call,
                    onClick = onAudioCall,
                )
                StreamSecondaryButton(
                    text = stringResource(R.string.chat_video_call),
                    onClick = onVideoCall,
                )
            }
            StreamPrimaryButton(
                text = if (result.alreadyInContacts) {
                    stringResource(R.string.contacts_remove_action)
                } else {
                    stringResource(R.string.contacts_add_action)
                },
                onClick = onToggleContact,
            )
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    onClick: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = StreamTheme.spacing.md, vertical = StreamTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StreamAvatar(
            imageUrl = contact.user.avatarUrl,
            fallbackLabel = contact.user.displayName,
            size = 52.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.localAlias ?: contact.user.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (Instant.now().epochSecond - contact.updatedAt.epochSecond < 120) {
                    stringResource(R.string.contacts_online)
                } else {
                    stringResource(R.string.contacts_last_seen_recently)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
            CircularIconPlate(
                icon = Icons.Filled.Call,
                onClick = onAudioCall,
            )
            StreamSecondaryButton(
                text = stringResource(R.string.chat_video_call),
                onClick = onVideoCall,
            )
        }
    }
}

@Composable
private fun DividerInset() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 84.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .height(1.dp),
    )
}

@Composable
private fun SearchStateCard(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = StreamTheme.spacing.md, vertical = StreamTheme.spacing.sm),
    )
}

@Preview
@Composable
private fun ContactsScreenPreview() {
    StreamGramTheme(darkTheme = false) {
        ContactsScreen(
            state = ContactsUiState(
                contacts = listOf(
                    Contact(
                        ownerUserId = "me",
                        contactUserId = "1",
                        user = User("1", "alice", "Alice Nova", "", "", 0, 0, 0),
                        createdAt = Instant.now().minusSeconds(60),
                        updatedAt = Instant.now().minusSeconds(60),
                    ),
                ),
            ),
            snackbarHostState = SnackbarHostState(),
            onQueryChanged = {},
            onContactSelected = {},
            onStartAudioCall = {},
            onStartVideoCall = {},
            onAddContact = {},
            onRemoveContact = {},
        )
    }
}
