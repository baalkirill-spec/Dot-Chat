package com.streamgram.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.AuthState
import com.streamgram.core.model.CallType
import com.streamgram.domain.usecase.ForwardPostUseCase
import com.streamgram.domain.usecase.ObserveAuthorizationStateUseCase
import com.streamgram.domain.usecase.ObserveChatsUseCase
import com.streamgram.domain.usecase.ObserveMessagesUseCase
import com.streamgram.domain.usecase.SendMessageUseCase
import com.streamgram.domain.usecase.StartCallUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChatsUseCase: ObserveChatsUseCase,
    observeMessagesUseCase: ObserveMessagesUseCase,
    observeAuthorizationStateUseCase: ObserveAuthorizationStateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val startCallUseCase: StartCallUseCase,
) : ViewModel() {
    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    private val draft = MutableStateFlow("")

    val uiState = combine(
        observeChatsUseCase(),
        observeMessagesUseCase(chatId),
        observeAuthorizationStateUseCase(),
        draft,
    ) { chats, messages, authState, currentDraft ->
        val chat = chats.firstOrNull { it.id == chatId }
        val currentUserId = (authState as? AuthState.Authenticated)?.session?.user?.id
        val callTargetUserId = chat?.members?.firstOrNull { member -> member.id != currentUserId }?.id
        ChatUiState(
            chat = chat,
            messages = messages,
            draft = currentDraft,
            callTargetUserId = callTargetUserId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState(),
    )

    fun onDraftChanged(value: String) {
        draft.value = value
    }

    fun sendMessage() {
        if (draft.value.isBlank()) return
        viewModelScope.launch {
            sendMessageUseCase(chatId = chatId, text = draft.value.trim())
            draft.value = ""
        }
    }

    suspend fun startCall(type: CallType): Boolean {
        val targetUserId = uiState.value.callTargetUserId ?: return false
        return runCatching { startCallUseCase(targetUserId, type) }.isSuccess
    }
}

@HiltViewModel
class ForwardPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChatsUseCase: ObserveChatsUseCase,
    private val forwardPostUseCase: ForwardPostUseCase,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val forwardedChatId = MutableStateFlow<String?>(null)

    val uiState = combine(
        observeChatsUseCase(),
        forwardedChatId,
    ) { chats, currentForwardedChatId ->
        ForwardPostUiState(
            postId = postId,
            chats = chats,
            forwardedChatId = currentForwardedChatId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ForwardPostUiState(postId = postId),
    )

    fun forwardToChat(chatId: String) {
        viewModelScope.launch {
            forwardPostUseCase(postId = postId, chatId = chatId)
            forwardedChatId.value = chatId
        }
    }
}
