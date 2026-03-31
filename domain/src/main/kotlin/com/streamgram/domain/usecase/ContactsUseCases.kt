package com.streamgram.domain.usecase

import com.streamgram.domain.repository.ContactsRepository
import javax.inject.Inject

class ObserveContactsUseCase @Inject constructor(
    private val repository: ContactsRepository,
) {
    operator fun invoke() = repository.observeContacts()
}

class SearchUsersUseCase @Inject constructor(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(query: String) = repository.searchUsers(query)
}

class AddContactUseCase @Inject constructor(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(userId: String, alias: String? = null) = repository.addContact(userId, alias)
}

class RemoveContactUseCase @Inject constructor(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(userId: String) = repository.removeContact(userId)
}

class StartChatWithContactUseCase @Inject constructor(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(userId: String) = repository.startChatWithContact(userId)
}
