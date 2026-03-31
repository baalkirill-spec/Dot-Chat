package com.streamgram.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.domain.usecase.ObserveChannelUseCase
import com.streamgram.domain.usecase.ObserveDiscoverChannelsUseCase
import com.streamgram.domain.usecase.ToggleFollowChannelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    observeDiscoverChannelsUseCase: ObserveDiscoverChannelsUseCase,
) : ViewModel() {
    val uiState = observeDiscoverChannelsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}

@HiltViewModel
class ChannelProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChannelUseCase: ObserveChannelUseCase,
    private val toggleFollowChannelUseCase: ToggleFollowChannelUseCase,
) : ViewModel() {
    private val channelId: String = checkNotNull(savedStateHandle["channelId"])

    val uiState = observeChannelUseCase(channelId)
        .map { channel -> ChannelProfileUiState(channel = channel) }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChannelProfileUiState(),
    )

    fun onToggleFollow(currentlySubscribed: Boolean) {
        viewModelScope.launch {
            toggleFollowChannelUseCase(channelId = channelId, subscribed = !currentlySubscribed)
        }
    }
}
