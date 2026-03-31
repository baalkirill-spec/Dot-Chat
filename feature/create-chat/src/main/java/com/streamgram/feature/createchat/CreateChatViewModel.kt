package com.streamgram.feature.createchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.ConversationVisibility
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.domain.usecase.CreateChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateChatViewModel @Inject constructor(
    private val createChatUseCase: CreateChatUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateChatUiState())
    val uiState = mutableState.asStateFlow()

    fun onTitleChanged(value: String) {
        mutableState.update { it.copy(title = value) }
    }

    fun onDescriptionChanged(value: String) {
        mutableState.update { it.copy(description = value) }
    }

    fun onVisibilityChanged(index: Int) {
        mutableState.update {
            it.copy(
                visibility = if (index == 0) {
                    ConversationVisibility.PUBLIC
                } else {
                    ConversationVisibility.PRIVATE
                },
            )
        }
    }

    fun createChat() {
        val snapshot = mutableState.value
        if (snapshot.title.isBlank() || snapshot.isSubmitting) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSubmitting = true) }
            val chatId = createChatUseCase(
                CreateChatRequest(
                    title = snapshot.title.trim(),
                    description = snapshot.description.trim(),
                    visibility = snapshot.visibility,
                ),
            )
            mutableState.update { it.copy(isSubmitting = false, createdChatId = chatId) }
        }
    }
}
