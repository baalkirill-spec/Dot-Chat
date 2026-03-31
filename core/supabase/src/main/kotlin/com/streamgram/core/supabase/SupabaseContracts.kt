package com.streamgram.core.supabase

import com.streamgram.core.model.ContactSearchResult
import com.streamgram.core.model.CreateChannelDraft
import com.streamgram.core.model.CreateChatDraft
import com.streamgram.core.model.ProfileSetupDraft
import com.streamgram.core.model.User
import javax.inject.Inject

data class DotChatSupabaseConfig(
    val url: String,
    val anonKey: String,
    val storageBucket: String = "media",
)

data class PhoneOtpRequest(
    val phoneNumber: String,
    val channel: PhoneOtpChannel = PhoneOtpChannel.SMS,
)

enum class PhoneOtpChannel {
    SMS,
    WHATSAPP,
}

interface SupabaseAuthGateway {
    suspend fun requestPhoneOtp(request: PhoneOtpRequest)
    suspend fun verifyPhoneOtp(phoneNumber: String, code: String): SupabaseAuthResult
    suspend fun refreshSession(refreshToken: String): SupabaseAuthSession
}

sealed interface SupabaseAuthResult {
    data class ExistingUser(val session: SupabaseAuthSession) : SupabaseAuthResult
    data class NewUser(val pendingUserId: String, val verifiedPhoneNumber: String) : SupabaseAuthResult
}

data class SupabaseAuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long?,
)

interface SupabaseProfileGateway {
    suspend fun completeProfileSetup(draft: ProfileSetupDraft): User
    suspend fun searchUsers(query: String): List<ContactSearchResult>
}

interface SupabaseChatsGateway {
    suspend fun createChat(draft: CreateChatDraft): String
    suspend fun createChannel(draft: CreateChannelDraft): String
}

class NoOpSupabaseConfigProvider @Inject constructor() {
    fun current(): DotChatSupabaseConfig = DotChatSupabaseConfig(
        url = "https://project.supabase.co",
        anonKey = "public-anon-key",
    )
}
