package com.streamgram.core.model

enum class CallType {
    AUDIO,
    VIDEO,
    GROUP_VIDEO,
    VOICE_ROOM,
}

enum class CallConnectionState {
    IDLE,
    CONNECTING,
    RINGING,
    CONNECTED,
    RECONNECTING,
    ENDED,
    FAILED,
}

data class CallParticipant(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val isMuted: Boolean,
    val isCameraEnabled: Boolean,
    val isScreenSharing: Boolean,
)

data class CallSession(
    val roomId: String,
    val roomName: String,
    val type: CallType,
    val connectionState: CallConnectionState,
    val participants: List<CallParticipant>,
)
