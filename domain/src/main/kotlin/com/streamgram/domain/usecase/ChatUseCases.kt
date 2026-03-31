package com.streamgram.domain.usecase

import com.streamgram.core.model.Chat
import com.streamgram.core.model.CreateChatRequest
import com.streamgram.core.model.Message
import com.streamgram.domain.repository.ChatsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveChatsUseCase @Inject constructor(
    private val repository: ChatsRepository,
) {
    operator fun invoke(): Flow<List<Chat>> = repository.observeChats()
}

class ObserveMessagesUseCase @Inject constructor(
    private val repository: ChatsRepository,
) {
    operator fun invoke(chatId: String): Flow<List<Message>> = repository.observeMessages(chatId)
}

class SendMessageUseCase @Inject constructor(
    private val repository: ChatsRepository,
) {
    suspend operator fun invoke(chatId: String, text: String) {
        repository.sendMessage(chatId = chatId, text = text)
    }
}

class CreateChatUseCase @Inject constructor(
    private val repository: ChatsRepository,
) {
    suspend operator fun invoke(request: CreateChatRequest): String {
        return repository.createChat(request)
    }
}

class ForwardPostUseCase @Inject constructor(
    private val repository: ChatsRepository,
) {
    suspend operator fun invoke(postId: String, chatId: String, note: String? = null) {
        repository.forwardPost(postId = postId, chatId = chatId, note = note)
    }
}
