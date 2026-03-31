package com.streamgram.domain.repository

import com.streamgram.core.model.Contact
import com.streamgram.core.model.ContactSearchResult
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun observeContacts(): Flow<List<Contact>>
    suspend fun searchUsers(query: String): List<ContactSearchResult>
    suspend fun addContact(userId: String, alias: String? = null)
    suspend fun removeContact(userId: String)
    suspend fun startChatWithContact(userId: String): String
}
