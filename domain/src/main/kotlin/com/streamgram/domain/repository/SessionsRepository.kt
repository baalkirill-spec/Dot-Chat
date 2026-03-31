package com.streamgram.domain.repository

import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.SecurityNotification
import kotlinx.coroutines.flow.Flow

interface SessionsRepository {
    fun observeSessions(): Flow<List<DeviceSession>>
    fun observeSecurityNotifications(): Flow<List<SecurityNotification>>
    suspend fun terminateSession(sessionId: String)
    suspend fun terminateOtherSessions()
}
