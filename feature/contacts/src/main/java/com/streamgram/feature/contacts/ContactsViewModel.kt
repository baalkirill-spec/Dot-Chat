package com.streamgram.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.CallType
import com.streamgram.domain.usecase.AddContactUseCase
import com.streamgram.domain.usecase.ObserveContactsUseCase
import com.streamgram.domain.usecase.RemoveContactUseCase
import com.streamgram.domain.usecase.SearchUsersUseCase
import com.streamgram.domain.usecase.StartCallUseCase
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
class ContactsViewModel @Inject constructor(
    observeContactsUseCase: ObserveContactsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val removeContactUseCase: RemoveContactUseCase,
    private val startChatWithContactUseCase: StartChatWithContactUseCase,
    private val startCallUseCase: StartCallUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val searchResults = MutableStateFlow(ContactsUiState().searchResults)
    private val isSearching = MutableStateFlow(false)
    private var searchJob: Job? = null

    val uiState = combine(
        observeContactsUseCase(),
        query,
        searchResults,
        isSearching,
    ) { contacts, currentQuery, currentResults, searching ->
        ContactsUiState(
            contacts = contacts,
            query = currentQuery,
            searchResults = currentResults,
            isSearching = searching,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ContactsUiState(),
    )

    fun onQueryChanged(value: String) {
        query.value = value
        searchJob?.cancel()
        if (value.isBlank()) {
            searchResults.value = emptyList()
            isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            isSearching.value = true
            delay(250)
            searchResults.value = searchUsersUseCase(value)
            isSearching.value = false
        }
    }

    fun addContact(userId: String) {
        viewModelScope.launch {
            addContactUseCase(userId)
            refreshSearch()
        }
    }

    fun removeContact(userId: String) {
        viewModelScope.launch {
            removeContactUseCase(userId)
            refreshSearch()
        }
    }

    suspend fun startChatWith(userId: String): String {
        return startChatWithContactUseCase(userId)
    }

    suspend fun startCallWith(userId: String, type: CallType): Boolean {
        return runCatching { startCallUseCase(userId, type) }.isSuccess
    }

    private suspend fun refreshSearch() {
        val currentQuery = query.value
        if (currentQuery.isNotBlank()) {
            searchResults.value = searchUsersUseCase(currentQuery)
        } else {
            searchResults.value = emptyList()
        }
    }
}
