package com.streamgram.feature.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.domain.usecase.ObserveChatsUseCase
import com.streamgram.domain.usecase.SearchChannelsUseCase
import com.streamgram.domain.usecase.SearchUsersUseCase
import com.streamgram.domain.usecase.StartChatWithContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatListViewModel @Inject constructor(
    observeChatsUseCase: ObserveChatsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val searchChannelsUseCase: SearchChannelsUseCase,
    private val startChatWithContactUseCase: StartChatWithContactUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isSearching = MutableStateFlow(false)
    private val peopleResults = MutableStateFlow(ChatListUiState().peopleResults)
    private val channelResults = MutableStateFlow(ChatListUiState().channelResults)
    private var searchJob: Job? = null

    val uiState = combine(
        observeChatsUseCase(),
        query,
        isSearching,
        peopleResults,
        channelResults,
    ) { chats, currentQuery, searching, people, channels ->
        ChatListUiState(
            chats = chats,
            query = currentQuery,
            isSearching = searching,
            peopleResults = people,
            channelResults = channels,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatListUiState(),
    )

    fun onQueryChanged(value: String) {
        query.value = value
        searchJob?.cancel()
        if (value.isBlank()) {
            peopleResults.value = emptyList()
            channelResults.value = emptyList()
            isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            isSearching.value = true
            delay(250) // debounce
            val people = runCatching { searchUsersUseCase(value) }.getOrDefault(emptyList())
            val channels = runCatching { searchChannelsUseCase(value) }.getOrDefault(emptyList())
            peopleResults.value = people
            channelResults.value = channels
            isSearching.value = false
        }
    }

    suspend fun startChatWith(userId: String): String {
        return startChatWithContactUseCase(userId)
    }
}
