package com.streamgram.feature.createchannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamgram.core.model.ConversationVisibility
import com.streamgram.core.model.CreateChannelRequest
import com.streamgram.domain.usecase.CreateChannelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateChannelViewModel @Inject constructor(
    private val createChannelUseCase: CreateChannelUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateChannelUiState())
    val uiState = mutableState.asStateFlow()

    fun onTitleChanged(value: String) {
        mutableState.update { it.copy(title = value) }
    }

    fun onHandleChanged(value: String) {
        mutableState.update { it.copy(handle = value.lowercase().replace(" ", "").removePrefix("@")) }
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

    fun createChannel() {
        val snapshot = mutableState.value
        if (snapshot.title.isBlank() || snapshot.isSubmitting) return
        if (snapshot.visibility == ConversationVisibility.PUBLIC && snapshot.handle.length < 4) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSubmitting = true) }
            val channelId = createChannelUseCase(
                CreateChannelRequest(
                    title = snapshot.title.trim(),
                    description = snapshot.description.trim(),
                    handle = snapshot.handle.takeIf { it.isNotBlank() },
                    visibility = snapshot.visibility,
                ),
            )
            mutableState.update { it.copy(isSubmitting = false, createdChannelId = channelId) }
        }
    }
}
