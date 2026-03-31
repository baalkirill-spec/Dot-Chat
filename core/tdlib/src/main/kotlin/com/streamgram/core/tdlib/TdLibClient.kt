package com.streamgram.core.tdlib

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TdLibClient {
    val lifecycleState: StateFlow<TdLibLifecycleState>
    val updates: Flow<TdLibUpdate>

    suspend fun start(parameters: TdLibParameters)
    suspend fun stop(graceful: Boolean = true)
    suspend fun <T : TdLibPayload> execute(request: TdLibRequest<T>): TdLibResult<T>
}

data class TdLibParameters(
    val apiId: Int,
    val apiHash: String,
    val databaseDirectory: String,
    val filesDirectory: String,
    val systemLanguageCode: String,
    val deviceModel: String,
    val applicationVersion: String,
    val useMessageDatabase: Boolean,
    val useSecretChats: Boolean,
)

enum class TdLibLifecycleState {
    IDLE,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED,
}

interface TdLibPayload

data class TdLibRequest<T : TdLibPayload>(
    val method: String,
    val payload: T,
    val requestId: String,
)

sealed interface TdLibResult<out T : TdLibPayload> {
    data class Success<T : TdLibPayload>(
        val value: T,
    ) : TdLibResult<T>

    data class Failure(
        val error: TdLibError,
    ) : TdLibResult<Nothing>
}

data class TdLibError(
    val code: Int,
    val message: String,
    val requestId: String? = null,
    val retriable: Boolean = false,
)

sealed interface TdLibUpdate {
    data class AuthorizationStateChanged(
        val state: String,
    ) : TdLibUpdate

    data class ConnectionStateChanged(
        val state: String,
    ) : TdLibUpdate

    data class ChatUpdated(
        val chatId: Long,
    ) : TdLibUpdate

    data class MessageUpdated(
        val chatId: Long,
        val messageId: Long,
    ) : TdLibUpdate

    data class FileUpdated(
        val fileId: Int,
    ) : TdLibUpdate

    data class CallUpdated(
        val callId: Int,
    ) : TdLibUpdate
}
