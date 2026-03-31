package com.streamgram.core.livekit

import android.content.Context
import com.streamgram.core.model.CallConnectionState
import com.streamgram.core.model.CallParticipant
import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LiveKitCloudTokenProvider(
    private val config: LiveKitCloudConfig,
) : LiveKitTokenProvider {
    override suspend fun issueGrant(
        roomName: String,
        participantIdentity: String,
        type: CallType,
    ): LiveKitAccessGrant = withContext(Dispatchers.IO) {
        require(config.tokenEndpoint.isNotBlank()) { "livekit_token_endpoint_missing" }
        val connection = URL(config.tokenEndpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        val payload = JSONObject()
            .put("roomName", roomName)
            .put("participantIdentity", participantIdentity)
            .put("type", type.name)
        connection.outputStream.bufferedWriter().use { writer ->
            writer.write(payload.toString())
        }
        val body = (if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        })?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (connection.responseCode !in 200..299) {
            error(body.ifBlank { "livekit_token_request_failed" })
        }
        val json = JSONObject(body)
        LiveKitAccessGrant(
            roomName = json.optString("roomName", roomName),
            participantIdentity = json.optString("participantIdentity", participantIdentity),
            token = json.getString("token"),
            type = type,
        )
    }
}

class LiveKitRoomControllerImpl(
    private val appContext: Context,
    private val config: LiveKitCloudConfig,
    private val tokenProvider: LiveKitTokenProvider,
) : LiveKitRoomController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableActiveCall = MutableStateFlow<CallSession?>(null)
    private var room: Room? = null
    private var roomEventsJob: Job? = null
    private var activeType: CallType = CallType.AUDIO

    override val activeCall: StateFlow<CallSession?> = mutableActiveCall.asStateFlow()

    init {
        LiveKit.init(appContext)
    }

    override suspend fun connectToGrant(grant: LiveKitAccessGrant) {
        require(config.url.isNotBlank()) { "livekit_url_missing" }
        disconnect()
        activeType = grant.type
        val createdRoom = LiveKit.create(appContext, RoomOptions())
        room = createdRoom
        mutableActiveCall.value = CallSession(
            roomId = grant.roomName,
            roomName = grant.roomName,
            type = grant.type,
            connectionState = CallConnectionState.CONNECTING,
            participants = emptyList(),
        )
        roomEventsJob = scope.launch {
            createdRoom.events.collect { event ->
                handleRoomEvent(createdRoom, event)
            }
        }
        try {
            createdRoom.connect(
                url = config.url,
                token = grant.token,
                options = ConnectOptions(
                    audio = true,
                    video = grant.type != CallType.AUDIO && grant.type != CallType.VOICE_ROOM,
                ),
            )
            mutableActiveCall.value = snapshot(createdRoom, CallConnectionState.CONNECTED)
        } catch (throwable: Throwable) {
            mutableActiveCall.value = CallSession(
                roomId = grant.roomName,
                roomName = grant.roomName,
                type = grant.type,
                connectionState = CallConnectionState.FAILED,
                participants = emptyList(),
            )
            throw throwable
        }
    }

    override suspend fun joinExistingRoom(roomName: String, participantIdentity: String, type: CallType) {
        val grant = tokenProvider.issueGrant(
            roomName = roomName,
            participantIdentity = participantIdentity,
            type = type,
        )
        connectToGrant(grant)
    }

    override suspend fun disconnect() {
        roomEventsJob?.cancel()
        roomEventsJob = null
        room?.disconnect()
        room?.release()
        room = null
        mutableActiveCall.value = null
    }

    private fun handleRoomEvent(room: Room, event: RoomEvent) {
        val nextState = when (event) {
            is RoomEvent.Connected -> CallConnectionState.CONNECTED
            is RoomEvent.Reconnecting -> CallConnectionState.RECONNECTING
            is RoomEvent.Reconnected -> CallConnectionState.CONNECTED
            is RoomEvent.Disconnected -> CallConnectionState.ENDED
            is RoomEvent.FailedToConnect -> CallConnectionState.FAILED
            else -> mutableActiveCall.value?.connectionState ?: CallConnectionState.CONNECTING
        }
        mutableActiveCall.value = snapshot(room, nextState)
        if (event is RoomEvent.Disconnected) {
            scope.launch {
                disconnect()
            }
        }
    }

    private fun snapshot(room: Room, state: CallConnectionState): CallSession {
        val roomName = room.name.orEmpty()
        val participants = buildList {
            add(room.localParticipant.toParticipant())
            addAll(room.remoteParticipants.values.map { participant -> participant.toParticipant() })
        }
        return CallSession(
            roomId = roomName,
            roomName = roomName,
            type = activeType,
            connectionState = state,
            participants = participants,
        )
    }

    private fun Participant.toParticipant(): CallParticipant {
        val participantId = identity?.toString().orEmpty()
        val participantName = name?.takeIf { it.isNotBlank() } ?: participantId
        return CallParticipant(
            userId = participantId,
            displayName = participantName,
            avatarUrl = attributes["avatarUrl"],
            isMuted = !isMicrophoneEnabled,
            isCameraEnabled = isCameraEnabled,
            isScreenSharing = isScreenShareEnabled,
        )
    }
}
