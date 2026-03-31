package com.streamgram.core.model

const val MAX_UPLOAD_FILE_SIZE_BYTES: Long = 200L * 1024L * 1024L

data class LocalAttachment(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val uri: String,
    val category: AttachmentCategory,
)

enum class AttachmentCategory {
    PHOTO,
    VIDEO,
    FILE,
    VOICE,
    STICKER,
}

sealed interface AttachmentValidationResult {
    data object Valid : AttachmentValidationResult

    data class FileTooLarge(
        val maxBytes: Long,
        val actualBytes: Long,
    ) : AttachmentValidationResult

    data class UnsupportedType(
        val mimeType: String,
    ) : AttachmentValidationResult
}

sealed interface UploadState {
    data object Idle : UploadState
    data class Preparing(
        val attachmentId: String,
    ) : UploadState

    data class Uploading(
        val attachmentId: String,
        val sentBytes: Long,
        val totalBytes: Long,
    ) : UploadState

    data class Completed(
        val attachmentId: String,
        val remoteUrl: String,
    ) : UploadState

    data class Failed(
        val attachmentId: String,
        val reason: UploadFailureReason,
    ) : UploadState
}

enum class UploadFailureReason {
    FILE_TOO_LARGE,
    NETWORK_ERROR,
    SERVER_REJECTED,
    CANCELLED,
}
