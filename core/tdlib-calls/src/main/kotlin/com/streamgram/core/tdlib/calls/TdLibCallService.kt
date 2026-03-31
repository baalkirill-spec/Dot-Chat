package com.streamgram.core.tdlib.calls

import kotlinx.coroutines.flow.Flow

interface TdLibCallService {
    fun observeActiveCalls(): Flow<List<TdLibCallSnapshot>>
    suspend fun startVoiceCall(userId: Long)
    suspend fun startVideoCall(userId: Long)
    suspend fun accept(callId: Int)
    suspend fun decline(callId: Int)
    suspend fun end(callId: Int)
}

data class TdLibCallSnapshot(
    val id: Int,
    val peerUserId: Long,
    val state: TdLibCallState,
    val isVideo: Boolean,
)

enum class TdLibCallState {
    RINGING,
    CONNECTING,
    ACTIVE,
    ENDED,
    FAILED,
}
