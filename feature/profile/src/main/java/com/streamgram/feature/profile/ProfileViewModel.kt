package com.streamgram.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.User
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val user: User? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
) : ViewModel() {
    val uiState = observeAuthorizationStateUseCase()
        .map { state ->
            ProfileUiState(
                user = (state as? AuthState.Authenticated)?.session?.user,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )
}
