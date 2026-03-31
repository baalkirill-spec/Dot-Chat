package com.streamgram.data.repository

import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.livekit.LiveKitRoomController
import com.streamgram.core.livekit.LiveKitTokenProvider
import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType
import com.streamgram.domain.repository.CallsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class LiveKitCallsRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val roomController: LiveKitRoomController,
    private val tokenProvider: LiveKitTokenProvider,
) : CallsRepository {
    override fun observeActiveCall(): Flow<CallSession?> = roomController.activeCall

    override suspend fun startCall(targetId: String, type: CallType) {
        val snapshot = preferences.preferences.first()
        val participantIdentity = snapshot.sessionUserId ?: error("missing_user_session")
        val roomName = buildRoomName(participantIdentity, targetId, type)
        val grant = tokenProvider.issueGrant(
            roomName = roomName,
            participantIdentity = participantIdentity,
            type = type,
        )
        roomController.connectToGrant(grant)
    }

    override suspend fun joinRoom(roomId: String) {
        val snapshot = preferences.preferences.first()
        val participantIdentity = snapshot.sessionUserId ?: error("missing_user_session")
        roomController.joinExistingRoom(
            roomName = roomId,
            participantIdentity = participantIdentity,
            type = roomController.activeCall.value?.type ?: CallType.AUDIO,
        )
    }

    override suspend fun leaveCurrentCall() {
        roomController.disconnect()
    }

    private fun buildRoomName(currentUserId: String, targetId: String, type: CallType): String {
        val members = listOf(currentUserId, targetId).sorted().joinToString(separator = "-")
        return "dotchat-${type.name.lowercase()}-$members"
    }
}
