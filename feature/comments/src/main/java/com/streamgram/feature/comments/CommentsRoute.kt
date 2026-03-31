@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.streamgram.feature.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort
import com.streamgram.core.ui.StreamAvatar

@Composable
fun CommentsRoute(
    onBack: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CommentsScreen(
        state = state,
        onBack = onBack,
        onDraftChanged = viewModel::onDraftChanged,
        onSubmit = viewModel::submitComment,
        onSortSelected = viewModel::onSortSelected,
        onLikeComment = viewModel::onLikeComment,
    )
}

@Composable
private fun CommentsScreen(
    state: CommentsUiState,
    onBack: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSortSelected: (CommentSort) -> Unit,
    onLikeComment: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Комментарии") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.draft,
                    onValueChange = onDraftChanged,
                    label = { Text("Написать комментарий") },
                    maxLines = 3,
                )
                IconButton(onClick = onSubmit) {
                    Icon(Icons.Filled.Send, contentDescription = "Отправить")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilterChip(
                    selected = state.sort == CommentSort.TOP,
                    onClick = { onSortSelected(CommentSort.TOP) },
                    label = { Text("Top") },
                )
                FilterChip(
                    selected = state.sort == CommentSort.NEW,
                    onClick = { onSortSelected(CommentSort.NEW) },
                    label = { Text("New") },
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.comments, key = Comment::id) { comment ->
                    CommentCard(
                        comment = comment,
                        onLike = { onLikeComment(comment.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentCard(
    comment: Comment,
    onLike: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StreamAvatar(
                    imageUrl = comment.author.avatarUrl,
                    fallbackLabel = comment.author.displayName,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.author.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = comment.author.handle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onLike) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Лайк")
                }
            }
            Text(
                text = comment.message,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "${comment.reactions.likeCount} likes · ${comment.replyCount} replies",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
