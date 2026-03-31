package com.streamgram.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.AuthState
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import com.streamgram.domain.usecase.RequestAccountDeletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeleteAccountUiState(
    val username: String? = null,
    val confirmationInput: String = "",
    val isProcessing: Boolean = false,
    val isDeleted: Boolean = false,
    val errorKey: String? = null,
) {
    val canConfirm: Boolean
        get() = !isProcessing &&
            !username.isNullOrBlank() &&
            confirmationInput.trim().equals(username.trim(), ignoreCase = true)
}

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
    private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
) : ViewModel() {
    private val transientState = MutableStateFlow(DeleteAccountUiState())

    val uiState = combine(
        observeAuthorizationStateUseCase(),
        transientState,
    ) { authState, transient ->
        val username = (authState as? AuthState.Authenticated)?.session?.user?.username
        transient.copy(username = username)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeleteAccountUiState(),
    )

    fun onConfirmationInputChanged(value: String) {
        transientState.value = transientState.value.copy(
            confirmationInput = value,
            errorKey = null,
        )
    }

    fun dismissError() {
        transientState.value = transientState.value.copy(errorKey = null)
    }

    fun confirmDeletion() {
        if (!uiState.value.canConfirm) return
        viewModelScope.launch {
            transientState.value = transientState.value.copy(isProcessing = true, errorKey = null)
            runCatching {
                requestAccountDeletionUseCase()
            }.onSuccess {
                transientState.value = transientState.value.copy(
                    isProcessing = false,
                    isDeleted = true,
                )
            }.onFailure { error ->
                transientState.value = transientState.value.copy(
                    isProcessing = false,
                    errorKey = error.message ?: "delete_account_failed",
                )
            }
        }
    }
}
