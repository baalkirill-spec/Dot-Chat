package com.streamgram.core.tdlib.messages

import kotlinx.coroutines.flow.Flow

interface TdLibMessageService {
    fun observeHistory(chatId: Long): Flow<List<TdLibMessageEnvelope>>
    suspend fun loadHistory(chatId: Long, fromMessageId: Long, limit: Int)
    suspend fun sendText(chatId: Long, text: String, replyToMessageId: Long? = null)
    suspend fun sendMedia(chatId: Long, media: TdLibOutgoingMedia, caption: String? = null)
    suspend fun editMessage(chatId: Long, messageId: Long, text: String)
    suspend fun deleteMessage(chatId: Long, messageId: Long, revoke: Boolean)
    suspend fun forwardMessages(chatId: Long, fromChatId: Long, messageIds: List<Long>)
}

data class TdLibMessageEnvelope(
    val chatId: Long,
    val messageId: Long,
    val previewText: String,
    val status: TdLibMessageStatus,
)

data class TdLibOutgoingMedia(
    val localPath: String,
    val mimeType: String,
)

enum class TdLibMessageStatus {
    SENDING,
    SENT,
    READ,
    FAILED,
}
