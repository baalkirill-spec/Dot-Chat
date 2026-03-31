package com.streamgram.feature.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSecondaryButton
import com.streamgram.core.i18n.R
import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.SecurityNotification
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsRoute(
    onBack: () -> Unit,
    viewModel: SessionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sessions_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
        item {
            StreamPrimaryButton(
                text = stringResource(R.string.sessions_terminate_others),
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::terminateOtherSessions,
            )
        }
        items(state.sessions, key = DeviceSession::id) { session ->
            StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = session.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${session.platform} | ${session.ipCountryHint ?: "--"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.sessions_last_active, formatEpoch(session.lastActiveAtEpochSeconds)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (session.isCurrent) {
                        Text(
                            text = stringResource(R.string.sessions_current_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        StreamSecondaryButton(
                            text = stringResource(R.string.sessions_terminate_one),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.terminateSession(session.id) },
                        )
                    }
                }
            }
        }
        if (state.securityNotifications.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.sessions_security_events),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(state.securityNotifications, key = SecurityNotification::id) { notification ->
                StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatEpoch(notification.createdAtEpochSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun formatEpoch(epochSeconds: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
    return formatter.format(Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()))
}
