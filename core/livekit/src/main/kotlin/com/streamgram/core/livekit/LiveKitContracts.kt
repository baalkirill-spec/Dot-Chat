package com.streamgram.core.livekit

import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType
import kotlinx.coroutines.flow.StateFlow

data class LiveKitCloudConfig(
    val url: String,
    val tokenEndpoint: String,
)

data class LiveKitAccessGrant(
    val roomName: String,
    val participantIdentity: String,
    val token: String,
    val type: CallType,
)

interface LiveKitTokenProvider {
    suspend fun issueGrant(roomName: String, participantIdentity: String, type: CallType): LiveKitAccessGrant
}

interface LiveKitRoomController {
    val activeCall: StateFlow<CallSession?>

    suspend fun connectToGrant(grant: LiveKitAccessGrant)
    suspend fun joinExistingRoom(roomName: String, participantIdentity: String, type: CallType)
    suspend fun disconnect()
}
