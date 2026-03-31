@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.streamgram.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamStatPill
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R
import com.streamgram.core.model.CallConnectionState
import com.streamgram.core.model.CallParticipant
import com.streamgram.core.model.CallType
import com.streamgram.core.ui.StreamAvatar

@Composable
fun CallsRoute(
    onBack: () -> Unit,
    viewModel: CallsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorKey) {
        state.errorKey?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    CallsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onLeaveCall = viewModel::leaveCall,
        onRetryJoin = viewModel::retryJoin,
    )
}

@Composable
private fun CallsScreen(
    state: CallsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onLeaveCall: () -> Unit,
    onRetryJoin: () -> Unit,
) {
    val activeCall = state.activeCall
    val callState = activeCall?.connectionState ?: CallConnectionState.IDLE
    val participants = activeCall?.participants.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeCall?.roomName ?: stringResource(R.string.call_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = connectionLabel(callState),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (activeCall != null || state.canRetryJoin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StreamTheme.spacing.lg, vertical = StreamTheme.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
                ) {
                    if (activeCall != null) {
                        StreamPrimaryButton(
                            text = stringResource(R.string.call_end_action),
                            modifier = Modifier.weight(1f),
                            onClick = onLeaveCall,
                        )
                    }
                    if (state.canRetryJoin && activeCall == null) {
                        StreamPrimaryButton(
                            text = stringResource(R.string.call_retry_action),
                            modifier = Modifier.weight(1f),
                            onClick = onRetryJoin,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(StreamTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
        ) {
            item {
                StreamSectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(StreamTheme.spacing.xl),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StreamStatPill(label = callTypeLabel(activeCall?.type ?: state.requestedType))
                            StreamStatPill(
                                label = stringResource(R.string.call_participants_count, participants.size),
                            )
                        }
                        if (state.isLoading || callState == CallConnectionState.CONNECTING || callState == CallConnectionState.RECONNECTING) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        Text(
                            text = callBodyLabel(callState),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (participants.isEmpty()) {
                item {
                    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.call_waiting_participants),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.call_participants_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(participants, key = CallParticipant::userId) { participant ->
                    ParticipantRow(participant = participant)
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: CallParticipant,
) {
    StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreamAvatar(
                imageUrl = participant.avatarUrl,
                fallbackLabel = participant.displayName,
                size = 52.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = participantMeta(participant),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StreamStatPill(label = participant.displayName.take(1).uppercase())
        }
    }
}

@Composable
private fun connectionLabel(state: CallConnectionState): String = when (state) {
    CallConnectionState.IDLE -> stringResource(R.string.call_state_idle)
    CallConnectionState.CONNECTING -> stringResource(R.string.call_state_connecting)
    CallConnectionState.RINGING -> stringResource(R.string.call_state_ringing)
    CallConnectionState.CONNECTED -> stringResource(R.string.call_state_connected)
    CallConnectionState.RECONNECTING -> stringResource(R.string.call_state_reconnecting)
    CallConnectionState.ENDED -> stringResource(R.string.call_state_ended)
    CallConnectionState.FAILED -> stringResource(R.string.call_state_failed)
}

@Composable
private fun callTypeLabel(type: CallType): String = when (type) {
    CallType.AUDIO -> stringResource(R.string.call_type_audio)
    CallType.VIDEO -> stringResource(R.string.call_type_video)
    CallType.GROUP_VIDEO -> stringResource(R.string.call_type_group_video)
    CallType.VOICE_ROOM -> stringResource(R.string.call_type_voice_room)
}

@Composable
private fun callBodyLabel(state: CallConnectionState): String = when (state) {
    CallConnectionState.IDLE -> stringResource(R.string.call_body_idle)
    CallConnectionState.CONNECTING -> stringResource(R.string.call_body_connecting)
    CallConnectionState.RINGING -> stringResource(R.string.call_body_ringing)
    CallConnectionState.CONNECTED -> stringResource(R.string.call_body_connected)
    CallConnectionState.RECONNECTING -> stringResource(R.string.call_body_reconnecting)
    CallConnectionState.ENDED -> stringResource(R.string.call_body_ended)
    CallConnectionState.FAILED -> stringResource(R.string.call_body_failed)
}

@Composable
private fun participantMeta(participant: CallParticipant): String {
    return buildList {
        add(
            stringResource(
                if (participant.isMuted) {
                    R.string.call_meta_muted
                } else {
                    R.string.call_meta_mic_on
                },
            ),
        )
        add(
            stringResource(
                if (participant.isCameraEnabled) {
                    R.string.call_meta_camera_on
                } else {
                    R.string.call_meta_camera_off
                },
            ),
        )
        if (participant.isScreenSharing) {
            add(stringResource(R.string.call_meta_screen_share))
        }
    }.joinToString(" • ")
}
