package com.streamgram.core.tdlib.auth

import com.streamgram.core.tdlib.TdLibParameters
import kotlinx.coroutines.flow.Flow

interface TdLibAuthService {
    val authorizationState: Flow<TdLibAuthorizationStage>
    val currentAccount: Flow<TdLibAccountInfo?>

    suspend fun initialize(parameters: TdLibParameters)
    suspend fun submitPhoneNumber(phoneNumber: String)
    suspend fun submitCode(code: String)
    suspend fun submitPassword(password: String)
    suspend fun requestQrLogin()
    suspend fun logOut()
}

sealed interface TdLibAuthorizationStage {
    data object WaitTdlibParameters : TdLibAuthorizationStage
    data object WaitPhoneNumber : TdLibAuthorizationStage
    data class WaitCode(
        val phoneNumber: String,
        val timeoutSeconds: Int,
    ) : TdLibAuthorizationStage

    data class WaitPassword(
        val passwordHint: String?,
    ) : TdLibAuthorizationStage

    data class Ready(
        val userId: Long?,
    ) : TdLibAuthorizationStage

    data class Error(
        val message: String,
        val retriable: Boolean,
    ) : TdLibAuthorizationStage
}

data class TdLibAccountInfo(
    val userId: Long,
    val displayName: String,
    val username: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val isPremium: Boolean,
)
