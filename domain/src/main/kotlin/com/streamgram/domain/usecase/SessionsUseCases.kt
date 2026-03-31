package com.streamgram.domain.usecase

import com.streamgram.domain.repository.SessionsRepository
import javax.inject.Inject

class ObserveSessionsUseCase @Inject constructor(
    private val repository: SessionsRepository,
) {
    operator fun invoke() = repository.observeSessions()
}

class ObserveSecurityNotificationsUseCase @Inject constructor(
    private val repository: SessionsRepository,
) {
    operator fun invoke() = repository.observeSecurityNotifications()
}

class TerminateSessionUseCase @Inject constructor(
    private val repository: SessionsRepository,
) {
    suspend operator fun invoke(sessionId: String) = repository.terminateSession(sessionId)
}

class TerminateOtherSessionsUseCase @Inject constructor(
    private val repository: SessionsRepository,
) {
    suspend operator fun invoke() = repository.terminateOtherSessions()
}
