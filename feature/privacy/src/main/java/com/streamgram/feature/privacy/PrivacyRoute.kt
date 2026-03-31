package com.streamgram.feature.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSettingsRow
import com.streamgram.core.i18n.R
import com.streamgram.core.model.PrivacyAudience

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyRoute(
    onBack: () -> Unit,
    viewModel: PrivacyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val settings = state.settings ?: return

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
                        text = stringResource(R.string.privacy_title),
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
            StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Column {
                    PrivacyRow(
                        title = stringResource(R.string.privacy_phone),
                        subtitle = stringResource(R.string.privacy_phone_subtitle),
                        audience = settings.phoneNumberVisibility,
                        onClick = viewModel::cyclePhoneVisibility,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_last_seen),
                        subtitle = stringResource(R.string.privacy_last_seen_subtitle),
                        audience = settings.lastSeenVisibility,
                        onClick = viewModel::cycleLastSeenVisibility,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_calls),
                        subtitle = stringResource(R.string.privacy_calls_subtitle),
                        audience = settings.whoCanCall,
                        onClick = viewModel::cycleWhoCanCall,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_messages),
                        subtitle = stringResource(R.string.privacy_messages_subtitle),
                        audience = settings.whoCanMessage,
                        onClick = viewModel::cycleWhoCanMessage,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_chat_invites),
                        subtitle = stringResource(R.string.privacy_chat_invites_subtitle),
                        audience = settings.whoCanAddToChats,
                        onClick = viewModel::cycleWhoCanAddToChats,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_avatar),
                        subtitle = stringResource(R.string.privacy_avatar_subtitle),
                        audience = settings.avatarVisibility,
                        onClick = viewModel::cycleAvatarVisibility,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_username),
                        subtitle = stringResource(R.string.privacy_username_subtitle),
                        audience = settings.usernameVisibility,
                        onClick = viewModel::cycleUsernameVisibility,
                    )
                    PrivacyRow(
                        title = stringResource(R.string.privacy_bio),
                        subtitle = stringResource(R.string.privacy_bio_subtitle),
                        audience = settings.bioVisibility,
                        onClick = viewModel::cycleBioVisibility,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyRow(
    title: String,
    subtitle: String,
    audience: PrivacyAudience,
    onClick: () -> Unit,
) {
    StreamSettingsRow(
        icon = Icons.Filled.Lock,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailing = {
            Text(
                text = when (audience) {
                    PrivacyAudience.EVERYONE -> stringResource(R.string.privacy_everyone)
                    PrivacyAudience.CONTACTS -> stringResource(R.string.privacy_contacts)
                    PrivacyAudience.NOBODY -> stringResource(R.string.privacy_nobody)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        },
    )
}
