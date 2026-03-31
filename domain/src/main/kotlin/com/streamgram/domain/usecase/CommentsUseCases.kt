package com.streamgram.domain.usecase

import com.streamgram.core.model.Comment
import com.streamgram.core.model.CommentSort
import com.streamgram.core.model.ReactionKind
import com.streamgram.domain.repository.CommentsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCommentsUseCase @Inject constructor(
    private val repository: CommentsRepository,
) {
    operator fun invoke(postId: String, sort: CommentSort): Flow<List<Comment>> {
        return repository.observeComments(postId = postId, sort = sort)
    }
}

class AddCommentUseCase @Inject constructor(
    private val repository: CommentsRepository,
) {
    suspend operator fun invoke(postId: String, message: String, parentCommentId: String? = null) {
        repository.addComment(postId = postId, message = message, parentCommentId = parentCommentId)
    }
}

class ReactToCommentUseCase @Inject constructor(
    private val repository: CommentsRepository,
) {
    suspend operator fun invoke(commentId: String, kind: ReactionKind, emoji: String? = null) {
        repository.react(commentId = commentId, kind = kind, emoji = emoji)
    }
}
