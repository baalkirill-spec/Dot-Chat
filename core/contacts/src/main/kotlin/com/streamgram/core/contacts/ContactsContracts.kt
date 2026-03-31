package com.streamgram.core.contacts

import com.streamgram.core.model.ContactSearchResult

data class UserSearchPresentation(
    val query: String,
    val results: List<ContactSearchResult>,
)
