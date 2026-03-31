package com.streamgram.core.network

import com.streamgram.core.model.MAX_UPLOAD_FILE_SIZE_BYTES

enum class BackendService {
    AUTH,
    USERS,
    CHATS,
    MESSAGES,
    CHANNELS,
    COMMENTS,
    REACTIONS,
    MEDIA,
    NOTIFICATIONS,
    SEARCH,
}

data class PaginationRequest(
    val cursor: String? = null,
    val pageSize: Int = 20,
)

data class PageEnvelope<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

sealed interface BackendResult<out T> {
    data class Success<T>(
        val value: T,
        val fromCache: Boolean = false,
    ) : BackendResult<T>

    data class Failure(
        val error: BackendError,
    ) : BackendResult<Nothing>
}

data class BackendError(
    val code: String,
    val message: String,
    val retryable: Boolean,
)

data class UploadPolicy(
    val maxFileSizeBytes: Long = MAX_UPLOAD_FILE_SIZE_BYTES,
    val supportsBackgroundUploads: Boolean = true,
    val chunkSizeBytes: Long = 5L * 1024L * 1024L,
)
