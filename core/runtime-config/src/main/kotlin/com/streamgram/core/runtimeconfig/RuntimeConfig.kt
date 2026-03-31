package com.streamgram.core.runtimeconfig

import com.streamgram.core.featureflags.FeatureFlagKey
import com.streamgram.core.network.BackendStrategy
import com.streamgram.core.network.CacheStrategy
import com.streamgram.core.network.EndpointEnvironment
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.network.TransportKind
import java.time.Instant
import kotlinx.coroutines.flow.Flow

data class RuntimeConfig(
    val schemaVersion: Int,
    val source: RuntimeConfigSource,
    val environment: EndpointEnvironment,
    val timeoutPolicy: TimeoutPolicyConfig,
    val retryPolicy: RetryPolicyConfig,
    val circuitBreaker: CircuitBreakerConfig,
    val transports: Map<NetworkFeature, TransportConfig>,
    val experiments: Map<String, String>,
    val remoteOverrides: Map<FeatureFlagKey, Boolean>,
    val fetchedAt: Instant?,
)

data class TimeoutPolicyConfig(
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long,
)

data class RetryPolicyConfig(
    val maxAttempts: Int,
    val baseDelayMs: Long,
    val maxDelayMs: Long,
    val multiplier: Double,
)

data class CircuitBreakerConfig(
    val failureThreshold: Int,
    val resetTimeoutMs: Long,
    val gracefulDegradationEnabled: Boolean,
)

data class TransportConfig(
    val transportKind: TransportKind,
    val backendStrategy: BackendStrategy,
    val cacheStrategy: CacheStrategy,
    val offlineFallbackEnabled: Boolean,
)

enum class RuntimeConfigSource {
    LOCAL_BUNDLED,
    LOCAL_OVERRIDE,
    REMOTE_SIGNED,
}

interface RuntimeConfigRepository {
    fun observeConfig(): Flow<RuntimeConfig>
    suspend fun current(): RuntimeConfig
    suspend fun refresh()
}
