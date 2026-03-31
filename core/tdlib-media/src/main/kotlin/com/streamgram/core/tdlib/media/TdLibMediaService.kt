package com.streamgram.core.tdlib.media

import com.streamgram.core.tdlib.TdLibError
import kotlinx.coroutines.flow.Flow

interface TdLibMediaService {
    fun observeDownloads(): Flow<List<TdLibMediaDownload>>
    suspend fun requestFile(fileId: Long, priority: Int = 1): Result<TdLibMediaFile>
    suspend fun cancelDownload(fileId: Long)
}

data class TdLibMediaFile(
    val fileId: Long,
    val localPath: String?,
    val remoteId: String?,
    val sizeBytes: Long,
    val mimeType: String?,
)

data class TdLibMediaDownload(
    val fileId: Long,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isCompleted: Boolean,
    val error: TdLibError? = null,
)
