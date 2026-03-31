package com.streamgram.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSettingsRow
import com.streamgram.core.designsystem.theme.DotChatTheme
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.designsystem.theme.DotChatBlue
import com.streamgram.core.designsystem.theme.DotChatGreen
import com.streamgram.core.designsystem.theme.DotChatOrange
import com.streamgram.core.designsystem.theme.DotChatPurple
import com.streamgram.core.i18n.R
import com.streamgram.core.model.User
import com.streamgram.core.ui.StreamAvatar

@Composable
fun ProfileRoute(
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSessions: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            horizontal = StreamTheme.spacing.lg,
            vertical = StreamTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(StreamTheme.spacing.xl),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
                ) {
                    StreamAvatar(
                        imageUrl = user?.avatarUrl,
                        fallbackLabel = user?.displayName ?: "Dot Chat",
                        size = 96.dp,
                    )
                    Text(
                        text = user?.displayName ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = user?.username?.let { "@$it" } ?: "@dotchat",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = user?.phoneNumber ?: " ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md)) {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_phone),
                        value = user?.phoneNumber ?: stringResource(R.string.profile_empty_phone),
                    )
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_username),
                        value = user?.username?.let { "@$it" } ?: stringResource(R.string.profile_empty_username),
                    )
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_account_status),
                        value = stringResource(R.string.profile_ready_status),
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.profile_manage_account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Column {
                    StreamSettingsRow(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.notifications_title),
                        subtitle = stringResource(R.string.settings_notifications_row_subtitle),
                        iconTint = DotChatOrange,
                        onClick = onOpenNotifications,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Lock,
                        title = stringResource(R.string.privacy_title),
                        subtitle = stringResource(R.string.settings_privacy_row_subtitle),
                        iconTint = DotChatGreen,
                        onClick = onOpenPrivacy,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.sessions_title),
                        subtitle = stringResource(R.string.settings_folders_row_subtitle),
                        iconTint = DotChatPurple,
                        onClick = onOpenSessions,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.settings_title),
                        subtitle = stringResource(R.string.profile_manage_account_body),
                        iconTint = DotChatBlue,
                        onClick = onOpenSettings,
                    )
                }
            }
        }
        item {
            StreamPrimaryButton(
                text = stringResource(R.string.profile_settings_action),
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ProfileRoutePreview() {
    DotChatTheme(darkTheme = false) {
        ProfileContentPreview(
            user = User(
                id = "preview",
                handle = "kirill",
                displayName = "Kirill",
                avatarUrl = "",
                bio = "",
                subscriptionCount = 0,
                chatCount = 0,
                savedPostCount = 0,
                username = "kirill",
                phoneNumber = "+7 902 568-82-77",
            ),
            onOpenSettings = {},
            onOpenNotifications = {},
            onOpenPrivacy = {},
            onOpenSessions = {},
        )
    }
}

@Composable
private fun ProfileContentPreview(
    user: User,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenSessions: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            horizontal = StreamTheme.spacing.lg,
            vertical = StreamTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.lg),
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(StreamTheme.spacing.xl),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
                ) {
                    StreamAvatar(
                        imageUrl = user.avatarUrl,
                        fallbackLabel = user.displayName,
                        size = 96.dp,
                    )
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = user.phoneNumber.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Column {
                    StreamSettingsRow(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.notifications_title),
                        subtitle = stringResource(R.string.settings_notifications_row_subtitle),
                        iconTint = DotChatOrange,
                        onClick = onOpenNotifications,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Lock,
                        title = stringResource(R.string.privacy_title),
                        subtitle = stringResource(R.string.settings_privacy_row_subtitle),
                        iconTint = DotChatGreen,
                        onClick = onOpenPrivacy,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.sessions_title),
                        subtitle = stringResource(R.string.settings_folders_row_subtitle),
                        iconTint = DotChatPurple,
                        onClick = onOpenSessions,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.settings_title),
                        subtitle = stringResource(R.string.profile_manage_account_body),
                        iconTint = DotChatBlue,
                        onClick = onOpenSettings,
                    )
                }
            }
        }
        item {
            StreamPrimaryButton(
                text = stringResource(R.string.profile_settings_action),
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSettings,
            )
        }
    }
}
