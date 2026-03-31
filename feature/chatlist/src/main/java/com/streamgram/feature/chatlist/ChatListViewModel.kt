package com.streamgram.feature.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.domain.usecase.ObserveChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ChatListViewModel @Inject constructor(
    observeChatsUseCase: ObserveChatsUseCase,
) : ViewModel() {
    val uiState = observeChatsUseCase()
        .map(::ChatListUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChatListUiState(),
        )
}
