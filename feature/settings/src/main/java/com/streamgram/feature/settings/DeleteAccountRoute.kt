package com.streamgram.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import com.streamgram.core.designsystem.component.StreamPrimaryButton
import com.streamgram.core.designsystem.component.StreamSecondaryButton
import com.streamgram.core.designsystem.component.StreamSectionCard
import com.streamgram.core.designsystem.component.StreamTextField
import com.streamgram.core.designsystem.theme.DotChatRed
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.R

@Composable
fun DeleteAccountRoute(
    onBack: () -> Unit,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isDeleted) {
        DeleteAccountSuccessContent()
        return
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.delete_account_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DotChatRed,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = DotChatRed,
                        )
                        Text(
                            text = stringResource(R.string.delete_account_warning),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DotChatRed,
                        )
                    }
                    Text(
                        text = stringResource(R.string.delete_account_consequences_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    ConsequenceItem(stringResource(R.string.delete_account_consequence_messages))
                    ConsequenceItem(stringResource(R.string.delete_account_consequence_contacts))
                    ConsequenceItem(stringResource(R.string.delete_account_consequence_channels))
                    ConsequenceItem(stringResource(R.string.delete_account_consequence_sessions))
                    ConsequenceItem(stringResource(R.string.delete_account_consequence_irreversible))
                }
            }
        }

        item {
            StreamSectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md)) {
                    Text(
                        text = stringResource(R.string.delete_account_confirm_prompt),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.username != null) {
                        Text(
                            text = "@${state.username}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    StreamTextField(
                        value = state.confirmationInput,
                        onValueChange = viewModel::onConfirmationInputChanged,
                        label = stringResource(R.string.delete_account_confirm_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.errorKey != null,
                    )
                    if (state.errorKey != null) {
                        Text(
                            text = stringResource(R.string.delete_account_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = DotChatRed,
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.sm)) {
                StreamPrimaryButton(
                    text = if (state.isProcessing) {
                        stringResource(R.string.delete_account_processing)
                    } else {
                        stringResource(R.string.delete_account_action)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canConfirm,
                    isLoading = state.isProcessing,
                    onClick = viewModel::confirmDeletion,
                )
                StreamSecondaryButton(
                    text = stringResource(R.string.delete_account_cancel),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                )
            }
        }
    }
}

@Composable
private fun ConsequenceItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StreamTheme.spacing.xs),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = DotChatRed,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeleteAccountSuccessContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(StreamTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.delete_account_success),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(StreamTheme.spacing.md))
        Text(
            text = stringResource(R.string.delete_account_warning),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
