package com.streamgram.feature.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.domain.usecase.ObserveSecurityNotificationsUseCase
import com.streamgram.domain.usecase.ObserveSessionsUseCase
import com.streamgram.domain.usecase.TerminateOtherSessionsUseCase
import com.streamgram.domain.usecase.TerminateSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SessionsViewModel @Inject constructor(
    observeSessionsUseCase: ObserveSessionsUseCase,
    observeSecurityNotificationsUseCase: ObserveSecurityNotificationsUseCase,
    private val terminateSessionUseCase: TerminateSessionUseCase,
    private val terminateOtherSessionsUseCase: TerminateOtherSessionsUseCase,
) : ViewModel() {
    val uiState = combine(
        observeSessionsUseCase(),
        observeSecurityNotificationsUseCase(),
    ) { sessions, notifications ->
        SessionsUiState(
            sessions = sessions,
            securityNotifications = notifications,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionsUiState(),
    )

    fun terminateSession(sessionId: String) {
        viewModelScope.launch { terminateSessionUseCase(sessionId) }
    }

    fun terminateOtherSessions() {
        viewModelScope.launch { terminateOtherSessionsUseCase() }
    }
}
