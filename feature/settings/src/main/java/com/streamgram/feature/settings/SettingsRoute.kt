package com.streamgram.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSettingsRow
import com.streamgram.core.designsystem.component.StreamSwitchCell
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.designsystem.theme.DotChatBlue
import com.streamgram.core.designsystem.theme.DotChatGreen
import com.streamgram.core.designsystem.theme.DotChatOrange
import com.streamgram.core.designsystem.theme.DotChatPurple
import com.streamgram.core.designsystem.theme.DotChatRed
import com.streamgram.core.i18n.R
import com.streamgram.core.ui.StreamAvatar

@Composable
fun SettingsRoute(
    onBack: (() -> Unit)?,
    onOpenLanguage: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSessions: () -> Unit,
    onOpenDeleteAccount: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val currentUser = state.currentUser
    val accountLine = buildString {
        currentUser?.username?.takeIf { it.isNotBlank() }?.let { append("@").append(it) }
        currentUser?.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
            if (isNotEmpty()) append(" / ")
            append(phone)
        }
    }.ifBlank { stringResource(R.string.profile_manage_account_body) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = StreamTheme.spacing.lg,
                vertical = StreamTheme.spacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StreamAvatar(
                            imageUrl = currentUser?.avatarUrl,
                            fallbackLabel = currentUser?.displayName ?: stringResource(R.string.app_name),
                            size = 72.dp,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = currentUser?.displayName ?: stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = accountLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                SettingsSection {
                    StreamSettingsRow(
                        icon = Icons.Filled.Lock,
                        title = stringResource(R.string.settings_privacy_row),
                        subtitle = stringResource(R.string.settings_privacy_row_subtitle),
                        iconTint = DotChatGreen,
                        onClick = onOpenPrivacy,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.settings_folders_row),
                        subtitle = stringResource(R.string.settings_folders_row_subtitle),
                        iconTint = DotChatBlue,
                        onClick = onOpenSessions,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.settings_language),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                        iconTint = DotChatGreen,
                        onClick = onOpenLanguage,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.settings_preferences),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            item {
                SettingsSection {
                    StreamSwitchCell(
                        title = stringResource(R.string.settings_media_autoplay),
                        subtitle = stringResource(R.string.settings_media_autoplay_subtitle),
                        checked = state.settings.mediaAutoplayEnabled,
                        onCheckedChange = viewModel::onMediaAutoplayChanged,
                    )
                    StreamSwitchCell(
                        title = stringResource(R.string.settings_channel_recommendations),
                        subtitle = stringResource(R.string.settings_channel_recommendations_subtitle),
                        checked = state.settings.channelRecommendationsEnabled,
                        onCheckedChange = viewModel::onChannelRecommendationsChanged,
                    )
                    StreamSwitchCell(
                        title = stringResource(R.string.settings_rich_animations),
                        subtitle = stringResource(R.string.settings_rich_animations_subtitle),
                        checked = state.settings.richAnimationsEnabled,
                        onCheckedChange = viewModel::onRichAnimationsChanged,
                    )
                    StreamSwitchCell(
                        title = stringResource(R.string.settings_power_saving),
                        subtitle = stringResource(R.string.settings_power_saving_subtitle),
                        checked = state.settings.powerSavingModeEnabled,
                        onCheckedChange = viewModel::onPowerSavingChanged,
                    )
                    StreamSwitchCell(
                        title = stringResource(R.string.settings_power_saving_auto),
                        subtitle = stringResource(R.string.settings_power_saving_auto_subtitle),
                        checked = state.settings.automaticPowerSavingEnabled,
                        onCheckedChange = viewModel::onAutomaticPowerSavingChanged,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.delete_account_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            item {
                SettingsSection {
                    StreamSettingsRow(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.settings_sign_out),
                        subtitle = stringResource(R.string.settings_sign_out_subtitle),
                        iconTint = DotChatRed,
                        onClick = viewModel::logout,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Person,
                        title = stringResource(R.string.delete_account_title),
                        subtitle = stringResource(R.string.delete_account_warning),
                        iconTint = DotChatRed,
                        onClick = onOpenDeleteAccount,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    content: @Composable () -> Unit,
) {
    StreamSectionCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        Column {
            content()
        }
    }
}
