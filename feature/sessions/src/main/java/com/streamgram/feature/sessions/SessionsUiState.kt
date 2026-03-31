package com.streamgram.feature.sessions

import com.streamgram.core.model.DeviceSession
import com.streamgram.core.model.SecurityNotification

data class SessionsUiState(
    val sessions: List<DeviceSession> = emptyList(),
    val securityNotifications: List<SecurityNotification> = emptyList(),
)
