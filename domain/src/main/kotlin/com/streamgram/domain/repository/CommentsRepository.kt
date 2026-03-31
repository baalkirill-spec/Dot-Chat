package com.streamgram.domain.repository

import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort
import com.streamgram.core.model.ReactionKind
import kotlinx.coroutines.flow.Flow

interface CommentsRepository {
    fun observeComments(postId: String, sort: CommentSort): Flow<List<Comment>>
    suspend fun addComment(postId: String, message: String, parentCommentId: String? = null)
    suspend fun react(commentId: String, kind: ReactionKind, emoji: String? = null)
}
