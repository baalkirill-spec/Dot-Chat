@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.streamgram.feature.channels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import coil3.compose.AsyncImage
import com.streamgram.core.i18n.R
import com.streamgram.core.model.Channel
import com.streamgram.core.ui.StreamAvatar

@Composable
fun ChannelsRoute(
    onOpenChannel: (String) -> Unit,
    onCreateChannel: () -> Unit,
    viewModel: ChannelsViewModel = hiltViewModel(),
) {
    val channels by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.spaces_title)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateChannel) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
        },
    ) { innerPadding ->
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.spaces_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.spaces_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(channels, key = Channel::id) { channel ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenChannel(channel.id) },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StreamAvatar(
                            imageUrl = channel.avatarUrl,
                            fallbackLabel = channel.title,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(channel.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(channel.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(channel.topics.joinToString(", "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text(
                            if (channel.isSubscribed) {
                                stringResource(R.string.channels_following_state)
                            } else {
                                stringResource(R.string.channels_discover)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelProfileRoute(
    onBack: () -> Unit,
    onForwardPost: (String) -> Unit,
    viewModel: ChannelProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val channel = state.channel
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(channel?.handle ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (channel == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AsyncImage(
                        model = channel.coverUrl,
                        contentDescription = channel.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StreamAvatar(channel.avatarUrl, channel.title)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(channel.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(channel.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { viewModel.onToggleFollow(channel.isSubscribed) }) {
                            Text(
                                if (channel.isSubscribed) {
                                    stringResource(R.string.channels_following_state)
                                } else {
                                    stringResource(R.string.channels_follow)
                                },
                            )
                        }
                    }
                    Text(
                        text = channel.topics.joinToString("   "),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.channel_posts_empty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.channel_posts_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
