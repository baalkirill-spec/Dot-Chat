package com.streamgram.feature.contacts

import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.model.Contact

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val query: String = "",
    val searchResults: List<ContactSearchResult> = emptyList(),
    val isSearching: Boolean = false,
)
