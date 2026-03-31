@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.streamgram.feature.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.streamgram.core.designsystem.component.StreamGlassCard
import com.streamgram.core.designsystem.component.StreamListCell
import com.streamgram.core.designsystem.component.StreamStatPill
import com.streamgram.core.designsystem.theme.Ink900
import com.streamgram.core.designsystem.theme.Ink950
import com.streamgram.core.designsystem.theme.StreamTheme
import com.streamgram.core.i18n.AppLanguage
import com.streamgram.core.i18n.R

@Composable
fun LanguageRoute(
    onBack: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_title)) },
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
                .background(Brush.verticalGradient(listOf(Ink950, Ink900, MaterialTheme.colorScheme.background)))
                .padding(innerPadding),
        ) {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(StreamTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(StreamTheme.spacing.md),
            ) {
                item {
                    StreamGlassCard {
                        Text(
                            text = stringResource(R.string.settings_language_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(state.languages, key = AppLanguage::tag) { language ->
                    StreamGlassCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)) {
                        StreamListCell(
                            title = if (language == AppLanguage.SYSTEM) {
                                stringResource(R.string.language_system)
                            } else {
                                language.nativeName
                            },
                            subtitle = language.tag,
                            trailing = {
                                if (state.selectedLanguageTag == language.tag) {
                                    StreamStatPill(label = stringResource(R.string.language_current))
                                }
                            },
                            onClick = { viewModel.selectLanguage(language.tag) },
                        )
                    }
                }
            }
        }
    }
}
