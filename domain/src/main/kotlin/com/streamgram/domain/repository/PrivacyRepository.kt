package com.streamgram.domain.repository

import com.streamgram.core.model.BlockedUser
import com.streamgram.core.model.PrivacySettings
import kotlinx.coroutines.flow.Flow

interface PrivacyRepository {
    fun observePrivacySettings(): Flow<PrivacySettings>
    fun observeBlockedUsers(): Flow<List<BlockedUser>>
    suspend fun updatePrivacySettings(settings: PrivacySettings)
    suspend fun blockUser(userId: String, reason: String? = null)
    suspend fun unblockUser(userId: String)
}
