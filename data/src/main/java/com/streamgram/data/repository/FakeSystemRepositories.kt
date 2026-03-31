package com.streamgram.data.repository

import com.streamgram.core.model.BlockedUser
import com.streamgram.core.model.CallConnectionState
import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType
import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.PrivacyAudience
import com.streamgram.core.model.PrivacySettings
import com.streamgram.core.model.SecurityNotification
import com.streamgram.domain.repository.CallsRepository
import com.streamgram.domain.repository.PrivacyRepository
import com.streamgram.domain.repository.SessionsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakePrivacyRepository @Inject constructor() : PrivacyRepository {
    private val settings = MutableStateFlow(
        PrivacySettings(
            phoneNumberVisibility = PrivacyAudience.CONTACTS,
            usernameVisibility = PrivacyAudience.EVERYONE,
            avatarVisibility = PrivacyAudience.EVERYONE,
            lastSeenVisibility = PrivacyAudience.CONTACTS,
            bioVisibility = PrivacyAudience.EVERYONE,
            whoCanAddToChats = PrivacyAudience.CONTACTS,
            whoCanCall = PrivacyAudience.CONTACTS,
            whoCanMessage = PrivacyAudience.EVERYONE,
        ),
    )
    private val blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())

    override fun observePrivacySettings(): Flow<PrivacySettings> = settings.asStateFlow()

    override fun observeBlockedUsers(): Flow<List<BlockedUser>> = blockedUsers.asStateFlow()

    override suspend fun updatePrivacySettings(settings: PrivacySettings) {
        this.settings.value = settings
    }

    override suspend fun blockUser(userId: String, reason: String?) {
        blockedUsers.update { current ->
            current + BlockedUser(
                userId = userId,
                createdAtEpochSeconds = System.currentTimeMillis() / 1000,
                reason = reason,
            )
        }
    }

    override suspend fun unblockUser(userId: String) {
        blockedUsers.update { current -> current.filterNot { it.userId == userId } }
    }
}

@Singleton
class FakeSessionsRepository @Inject constructor() : SessionsRepository {
    private val sessions = MutableStateFlow(
        listOf(
            DeviceSession(
                id = "session-current",
                deviceName = "Pixel 9 Pro",
                platform = "Android 16",
                ipCountryHint = "RU",
                isCurrent = true,
                lastActiveAtEpochSeconds = System.currentTimeMillis() / 1000,
                signedInAtEpochSeconds = System.currentTimeMillis() / 1000 - 86_400,
            ),
            DeviceSession(
                id = "session-web",
                deviceName = "Chrome on Windows",
                platform = "Web",
                ipCountryHint = "DE",
                isCurrent = false,
                lastActiveAtEpochSeconds = System.currentTimeMillis() / 1000 - 2_100,
                signedInAtEpochSeconds = System.currentTimeMillis() / 1000 - 604_800,
            ),
        ),
    )
    private val securityNotifications = MutableStateFlow(
        listOf(
            SecurityNotification(
                id = "security-1",
                title = "New login detected",
                body = "Chrome on Windows signed in and received a fresh session.",
                createdAtEpochSeconds = System.currentTimeMillis() / 1000 - 2_100,
            ),
        ),
    )

    override fun observeSessions(): Flow<List<DeviceSession>> = sessions.asStateFlow()

    override fun observeSecurityNotifications(): Flow<List<SecurityNotification>> =
        securityNotifications.asStateFlow()

    override suspend fun terminateSession(sessionId: String) {
        sessions.update { current -> current.filterNot { it.id == sessionId || (it.isCurrent && it.id == sessionId) } }
    }

    override suspend fun terminateOtherSessions() {
        sessions.update { current -> current.filter { it.isCurrent } }
    }
}

@Singleton
class FakeCallsRepository @Inject constructor() : CallsRepository {
    private val activeCall = MutableStateFlow<CallSession?>(null)

    override fun observeActiveCall(): Flow<CallSession?> = activeCall.asStateFlow()

    override suspend fun startCall(targetId: String, type: CallType) {
        activeCall.value = CallSession(
            roomId = "room-$targetId",
            roomName = "dotchat-$targetId",
            type = type,
            connectionState = CallConnectionState.CONNECTING,
            participants = emptyList(),
        )
    }

    override suspend fun joinRoom(roomId: String) {
        activeCall.update { current ->
            current?.copy(roomId = roomId, connectionState = CallConnectionState.CONNECTED)
        }
    }

    override suspend fun leaveCurrentCall() {
        activeCall.value = null
    }
}
