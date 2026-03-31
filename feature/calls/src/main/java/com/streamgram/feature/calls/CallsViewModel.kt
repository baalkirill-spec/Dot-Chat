package com.streamgram.feature.calls

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.CallType
import com.streamgram.domain.usecase.JoinCallUseCase
import com.streamgram.domain.usecase.LeaveCurrentCallUseCase
import com.streamgram.domain.usecase.ObserveActiveCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CallsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeActiveCallUseCase: ObserveActiveCallUseCase,
    private val joinCallUseCase: JoinCallUseCase,
    private val leaveCurrentCallUseCase: LeaveCurrentCallUseCase,
) : ViewModel() {
    private val requestedType = savedStateHandle.get<String>("type")
        ?.let(::parseCallType)
        ?: CallType.AUDIO
    private val roomId: String? = savedStateHandle["roomId"]

    private val transientState = MutableStateFlow(
        CallsUiState(
            requestedType = requestedType,
            canRetryJoin = !roomId.isNullOrBlank(),
        ),
    )

    val uiState = combine(
        observeActiveCallUseCase(),
        transientState,
    ) { activeCall, transient ->
        transient.copy(activeCall = activeCall)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CallsUiState(
            requestedType = requestedType,
            canRetryJoin = !roomId.isNullOrBlank(),
        ),
    )

    init {
        if (!roomId.isNullOrBlank()) {
            joinRoom(roomId)
        }
    }

    fun dismissError() {
        transientState.update { it.copy(errorKey = null) }
    }

    fun leaveCall() {
        viewModelScope.launch {
            transientState.update { it.copy(isLoading = true, errorKey = null) }
            runCatching { leaveCurrentCallUseCase() }
                .onFailure { error ->
                    transientState.update {
                        it.copy(
                            isLoading = false,
                            errorKey = error.message ?: "call_leave_failed",
                        )
                    }
                }
                .onSuccess {
                    transientState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun retryJoin() {
        roomId?.let(::joinRoom)
    }

    private fun joinRoom(roomId: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isLoading = true, errorKey = null) }
            runCatching { joinCallUseCase(roomId) }
                .onFailure { error ->
                    transientState.update {
                        it.copy(
                            isLoading = false,
                            errorKey = error.message ?: "call_join_failed",
                        )
                    }
                }
                .onSuccess {
                    transientState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun parseCallType(raw: String): CallType {
        return runCatching { CallType.valueOf(raw.uppercase()) }.getOrDefault(CallType.AUDIO)
    }
}
