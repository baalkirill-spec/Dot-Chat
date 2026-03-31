package com.streamgram.domain.repository

import com.streamgram.core.model.AuthState
import com.streamgram.core.model.ProfileSetupDraft
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthorizationState(): Flow<AuthState>
    suspend fun markWelcomeSeen()
    suspend fun submitPhoneNumber(phoneNumber: String, emailAddress: String)
    suspend fun submitCode(code: String)
    suspend fun resendCode()
    suspend fun submitPassword(password: String)
    suspend fun submitProfileSetup(draft: ProfileSetupDraft)
    suspend fun logout()

    /**
     * Requests soft-deletion of the current user account.
     * Sets account_status to 'pending_deletion', invalidates sessions,
     * and clears local state. Throws on failure.
     */
    suspend fun requestAccountDeletion()
}
