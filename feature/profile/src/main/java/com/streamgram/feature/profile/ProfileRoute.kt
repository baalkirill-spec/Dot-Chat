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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamSettingsRow
import com.streamgram.core.designsystem.theme.DotChatTheme
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.designsystem.theme.Slate500
import com.streamgram.core.i18n.R
import com.streamgram.core.ui.StreamAvatar

@Composable
fun ProfileRoute(
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
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
                text = stringResource(R.string.account_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        // ── Current account card ─────────────────────────────────────────
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
                        size = 80.dp,
                    )
                    Text(
                        text = user?.displayName ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = user?.username?.let { "@$it" } ?: "@dotchat",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── Settings / Account actions ───────────────────────────────────
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                Column {
                    StreamSettingsRow(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.settings_title),
                        subtitle = stringResource(R.string.profile_manage_account_body),
                        iconTint = Slate500,
                        onClick = onOpenSettings,
                    )
                    StreamSettingsRow(
                        icon = Icons.Filled.Notifications,
                        title = stringResource(R.string.notifications_title),
                        subtitle = stringResource(R.string.settings_notifications_row_subtitle),
                        iconTint = Slate500,
                        onClick = onOpenNotifications,
                    )
                }
            }
        }

        // ── Add another account (honest entry point) ─────────────────────
        item {
            StreamSectionCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                StreamSettingsRow(
                    icon = Icons.Filled.Person,
                    title = stringResource(R.string.account_add_another),
                    subtitle = stringResource(R.string.account_add_another_body),
                    iconTint = Slate500,
                    onClick = { /* TODO: multi-account architecture — navigate to add-account flow */ },
                )
            }
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
private fun AccountRoutePreview() {
    DotChatTheme(darkTheme = false) {
        ProfileRoute(
            onOpenSettings = {},
            onOpenNotifications = {},
        )
    }
}

