package com.streamgram.core.security

import com.streamgram.core.model.DeviceSession
import kotlinx.coroutines.flow.Flow

interface SessionSecurityCoordinator {
    fun observeTrustedSessions(): Flow<List<DeviceSession>>
    suspend fun clearLocalSecrets()
}

data class SecurityPlan(
    val supportsBiometricUnlock: Boolean,
    val supportsEncryptedMediaCache: Boolean,
    val supportsPerDeviceRevocation: Boolean,
)
