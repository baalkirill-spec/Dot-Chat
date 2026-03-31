package com.streamgram.data.fake

import com.streamgram.core.common.TimeProvider
import com.streamgram.core.featureflags.FeatureFlagKey
import com.streamgram.core.network.BackendStrategy
import com.streamgram.core.network.CacheStrategy
import com.streamgram.core.network.EndpointEnvironment
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.network.TransportKind
import com.streamgram.core.runtimeconfig.CircuitBreakerConfig
import com.streamgram.core.runtimeconfig.RetryPolicyConfig
import com.streamgram.core.runtimeconfig.RuntimeConfig
import com.streamgram.core.runtimeconfig.RuntimeConfigRepository
import com.streamgram.core.runtimeconfig.RuntimeConfigSource
import com.streamgram.core.runtimeconfig.TimeoutPolicyConfig
import com.streamgram.core.runtimeconfig.TransportConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeRuntimeConfigRepository @Inject constructor(
    timeProvider: TimeProvider,
) : RuntimeConfigRepository {
    private val state = MutableStateFlow(
        RuntimeConfig(
            schemaVersion = 1,
            source = RuntimeConfigSource.LOCAL_BUNDLED,
            environment = EndpointEnvironment.PROD,
            timeoutPolicy = TimeoutPolicyConfig(10_000, 15_000, 15_000),
            retryPolicy = RetryPolicyConfig(maxAttempts = 3, baseDelayMs = 250, maxDelayMs = 1_500, multiplier = 2.0),
            circuitBreaker = CircuitBreakerConfig(failureThreshold = 4, resetTimeoutMs = 8_000, gracefulDegradationEnabled = true),
            transports = mapOf(
                NetworkFeature.FEED to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.STALE_WHILE_REVALIDATE, true),
                NetworkFeature.COMMENTS to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
                NetworkFeature.CHATS to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.NETWORK_FIRST, true),
                NetworkFeature.RECOMMENDATIONS to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
                NetworkFeature.MEDIA_PREFETCH to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
                NetworkFeature.CHANNELS to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
                NetworkFeature.ACTIVITY to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
                NetworkFeature.PROFILE to TransportConfig(TransportKind.FAKE, BackendStrategy.FAKE_BACKEND, CacheStrategy.CACHE_FIRST, true),
            ),
            experiments = mapOf("recommendation_mvp_v1" to "weighted_signals"),
            remoteOverrides = mapOf(
                FeatureFlagKey.AUTOPLAY to true,
                FeatureFlagKey.COMMENTS to true,
                FeatureFlagKey.RECOMMENDED_FEED to true,
            ),
            fetchedAt = timeProvider.now(),
        ),
    )

    override fun observeConfig(): Flow<RuntimeConfig> = state.asStateFlow()

    override suspend fun current(): RuntimeConfig = state.value

    override suspend fun refresh() = Unit

    fun snapshot(): RuntimeConfig = state.value
}
