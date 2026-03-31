@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.streamgram.feature.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.CommentSort
import com.streamgram.core.model.ReactionKind
import com.streamgram.domain.usecase.AddCommentUseCase
import com.streamgram.domain.usecase.ObserveCommentsUseCase
import com.streamgram.domain.usecase.ReactToCommentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCommentsUseCase: ObserveCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val reactToCommentUseCase: ReactToCommentUseCase,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val sort = MutableStateFlow(CommentSort.TOP)
    private val draft = MutableStateFlow("")

    val uiState = combine(
        sort,
        draft,
        sort.flatMapLatest { currentSort -> observeCommentsUseCase(postId = postId, sort = currentSort) },
    ) { currentSort, currentDraft, comments ->
        CommentsUiState(
            postId = postId,
            sort = currentSort,
            comments = comments,
            draft = currentDraft,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CommentsUiState(postId = postId),
    )

    fun onSortSelected(selected: CommentSort) {
        sort.value = selected
    }

    fun onDraftChanged(value: String) {
        draft.value = value
    }

    fun submitComment() {
        if (draft.value.isBlank()) return
        viewModelScope.launch {
            addCommentUseCase(postId = postId, message = draft.value.trim())
            draft.value = ""
        }
    }

    fun onLikeComment(commentId: String) {
        viewModelScope.launch {
            reactToCommentUseCase(commentId = commentId, kind = ReactionKind.LIKE)
        }
    }
}
