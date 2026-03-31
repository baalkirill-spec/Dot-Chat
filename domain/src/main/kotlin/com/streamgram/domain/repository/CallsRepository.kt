package com.streamgram.domain.repository

import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType
import kotlinx.coroutines.flow.Flow

interface CallsRepository {
    fun observeActiveCall(): Flow<CallSession?>
    suspend fun startCall(targetId: String, type: CallType)
    suspend fun joinRoom(roomId: String)
    suspend fun leaveCurrentCall()
}
