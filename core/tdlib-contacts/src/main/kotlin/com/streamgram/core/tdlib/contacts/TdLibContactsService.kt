package com.streamgram.core.tdlib.contacts

import com.streamgram.core.model.Friend
import kotlinx.coroutines.flow.Flow

interface TdLibContactsService {
    fun observeContacts(): Flow<List<Friend>>
    suspend fun syncContacts()
    suspend fun searchContacts(query: String): List<Friend>
}
