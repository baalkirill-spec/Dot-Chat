package com.streamgram.feature.comments

import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort

data class CommentsUiState(
    val postId: String = "",
    val sort: CommentSort = CommentSort.TOP,
    val comments: List<Comment> = emptyList(),
    val draft: String = "",
)
