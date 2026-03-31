package com.streamgram.data.repository

import android.util.Log
import com.streamgram.core.common.AppDispatchers
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.featureflags.FeatureFlag
import com.streamgram.core.featureflags.FeatureFlagKey
import com.streamgram.core.featureflags.FeatureFlagRepository
import com.streamgram.core.featureflags.FlagSource
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
import com.streamgram.core.telemetry.RequestTrace
import com.streamgram.core.telemetry.TelemetryEvent
import com.streamgram.core.telemetry.TelemetryLogger
import com.streamgram.data.runtime.AndroidBatteryStateMonitor
import com.streamgram.data.runtime.DevicePowerState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class PreferencesRuntimeConfigRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val batteryStateMonitor: AndroidBatteryStateMonitor,
    private val timeProvider: TimeProvider,
    dispatchers: AppDispatchers,
) : RuntimeConfigRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val state = combine(
        preferences.preferences,
        batteryStateMonitor.observeState(),
    ) { snapshot, powerState ->
        val powerSavingActive = snapshot.powerSavingModeEnabled ||
            snapshot.automaticPowerSavingEnabled && powerState.isBatteryLow ||
            powerState.systemPowerSaveEnabled
        val autoplayEnabled = snapshot.mediaAutoplayEnabled && !powerSavingActive
        val recommendationsEnabled = snapshot.channelRecommendationsEnabled && !powerSavingActive
        val reducedMotion = !snapshot.richAnimationsEnabled || powerSavingActive

        RuntimeConfig(
            schemaVersion = 2,
            source = if (powerSavingActive || !snapshot.richAnimationsEnabled || !snapshot.mediaAutoplayEnabled) {
                RuntimeConfigSource.LOCAL_OVERRIDE
            } else {
                RuntimeConfigSource.LOCAL_BUNDLED
            },
            environment = EndpointEnvironment.PROD,
            timeoutPolicy = if (powerSavingActive) {
                TimeoutPolicyConfig(connectTimeoutMs = 8_000, readTimeoutMs = 12_000, writeTimeoutMs = 12_000)
            } else {
                TimeoutPolicyConfig(connectTimeoutMs = 10_000, readTimeoutMs = 15_000, writeTimeoutMs = 15_000)
            },
            retryPolicy = if (powerSavingActive) {
                RetryPolicyConfig(maxAttempts = 2, baseDelayMs = 350, maxDelayMs = 1_000, multiplier = 1.8)
            } else {
                RetryPolicyConfig(maxAttempts = 3, baseDelayMs = 250, maxDelayMs = 1_500, multiplier = 2.0)
            },
            circuitBreaker = CircuitBreakerConfig(
                failureThreshold = if (powerSavingActive) 3 else 4,
                resetTimeoutMs = if (powerSavingActive) 12_000 else 8_000,
                gracefulDegradationEnabled = true,
            ),
            transports = buildTransportMap(
                powerSavingActive = powerSavingActive,
                recommendationsEnabled = recommendationsEnabled,
            ),
            experiments = mapOf(
                "motion_profile" to if (reducedMotion) "reduced" else "full",
                "power_saving" to if (powerSavingActive) "enabled" else "disabled",
                "battery_percent" to powerState.batteryPercent.toString(),
            ),
            remoteOverrides = mapOf(
                FeatureFlagKey.AUTOPLAY to autoplayEnabled,
                FeatureFlagKey.RECOMMENDED_FEED to recommendationsEnabled,
            ),
            fetchedAt = timeProvider.now(),
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = bundledConfig(timeProvider = timeProvider),
    )

    override fun observeConfig(): Flow<RuntimeConfig> = state

    override suspend fun current(): RuntimeConfig = state.first()

    override suspend fun refresh() = Unit

    fun snapshot(): RuntimeConfig = state.value

    private fun buildTransportMap(
        powerSavingActive: Boolean,
        recommendationsEnabled: Boolean,
    ): Map<NetworkFeature, TransportConfig> {
        val defaultRealTime = TransportConfig(
            transportKind = TransportKind.REAL,
            backendStrategy = BackendStrategy.FIRST_PARTY_API,
            cacheStrategy = CacheStrategy.NETWORK_FIRST,
            offlineFallbackEnabled = true,
        )
        return mapOf(
            NetworkFeature.FEED to TransportConfig(
                transportKind = if (recommendationsEnabled) TransportKind.REAL else TransportKind.CACHE_ONLY,
                backendStrategy = BackendStrategy.FIRST_PARTY_API,
                cacheStrategy = if (recommendationsEnabled) CacheStrategy.STALE_WHILE_REVALIDATE else CacheStrategy.CACHE_ONLY,
                offlineFallbackEnabled = true,
            ),
            NetworkFeature.COMMENTS to defaultRealTime,
            NetworkFeature.CHATS to defaultRealTime,
            NetworkFeature.RECOMMENDATIONS to TransportConfig(
                transportKind = if (recommendationsEnabled) TransportKind.REAL else TransportKind.CACHE_ONLY,
                backendStrategy = BackendStrategy.FIRST_PARTY_API,
                cacheStrategy = if (recommendationsEnabled) CacheStrategy.CACHE_FIRST else CacheStrategy.CACHE_ONLY,
                offlineFallbackEnabled = true,
            ),
            NetworkFeature.MEDIA_PREFETCH to TransportConfig(
                transportKind = if (powerSavingActive) TransportKind.CACHE_ONLY else TransportKind.REAL,
                backendStrategy = BackendStrategy.FIRST_PARTY_API,
                cacheStrategy = if (powerSavingActive) CacheStrategy.CACHE_ONLY else CacheStrategy.CACHE_FIRST,
                offlineFallbackEnabled = !powerSavingActive,
            ),
            NetworkFeature.CHANNELS to defaultRealTime,
            NetworkFeature.ACTIVITY to defaultRealTime.copy(cacheStrategy = CacheStrategy.STALE_WHILE_REVALIDATE),
            NetworkFeature.PROFILE to defaultRealTime.copy(cacheStrategy = CacheStrategy.CACHE_FIRST),
        )
    }
}

@Singleton
class PreferencesFeatureFlagRepository @Inject constructor(
    private val preferences: UserPreferencesDataSource,
    private val runtimeConfigRepository: RuntimeConfigRepository,
) : FeatureFlagRepository {
    override fun observeFlags(): Flow<Map<FeatureFlagKey, FeatureFlag>> {
        return combine(
            preferences.preferences,
            runtimeConfigRepository.observeConfig(),
        ) { snapshot, runtimeConfig ->
            val overrides = runtimeConfig.remoteOverrides
            mapOf(
                FeatureFlagKey.AUTOPLAY to buildFlag(
                    key = FeatureFlagKey.AUTOPLAY,
                    enabled = overrides[FeatureFlagKey.AUTOPLAY] ?: snapshot.mediaAutoplayEnabled,
                    overridden = FeatureFlagKey.AUTOPLAY in overrides,
                ),
                FeatureFlagKey.COMMENTS to buildFlag(FeatureFlagKey.COMMENTS, enabled = true, overridden = false),
                FeatureFlagKey.LIKE_DISLIKE to buildFlag(FeatureFlagKey.LIKE_DISLIKE, enabled = true, overridden = false),
                FeatureFlagKey.EMOJI_REACTIONS to buildFlag(FeatureFlagKey.EMOJI_REACTIONS, enabled = true, overridden = false),
                FeatureFlagKey.RECOMMENDED_FEED to buildFlag(
                    key = FeatureFlagKey.RECOMMENDED_FEED,
                    enabled = overrides[FeatureFlagKey.RECOMMENDED_FEED] ?: snapshot.channelRecommendationsEnabled,
                    overridden = FeatureFlagKey.RECOMMENDED_FEED in overrides,
                ),
                FeatureFlagKey.FOLLOWS to buildFlag(FeatureFlagKey.FOLLOWS, enabled = true, overridden = false),
            )
        }
    }

    override fun observeFlag(key: FeatureFlagKey): Flow<FeatureFlag> {
        return observeFlags().map { flags -> flags.getValue(key) }
    }

    override suspend fun refresh() {
        runtimeConfigRepository.refresh()
    }

    private fun buildFlag(
        key: FeatureFlagKey,
        enabled: Boolean,
        overridden: Boolean,
    ): FeatureFlag {
        return FeatureFlag(
            key = key,
            enabled = enabled,
            source = if (overridden) FlagSource.LOCAL_OVERRIDE else FlagSource.LOCAL_DEFAULT,
        )
    }
}

@Singleton
class AndroidTelemetryLogger @Inject constructor() : TelemetryLogger {
    override suspend fun log(event: TelemetryEvent) {
        Log.i("DotChatTelemetry", "event=${event.name} occurredAt=${event.occurredAt}")
    }

    override suspend fun logRequest(trace: RequestTrace) {
        Log.i(
            "DotChatNetwork",
            "request=${trace.requestId} feature=${trace.feature} endpoint=${trace.endpointHost} status=${trace.statusBucket} retries=${trace.retryCount} latencyMs=${trace.latencyMs}",
        )
    }
}

private fun bundledConfig(timeProvider: TimeProvider): RuntimeConfig {
    val defaultTransport = TransportConfig(
        transportKind = TransportKind.REAL,
        backendStrategy = BackendStrategy.FIRST_PARTY_API,
        cacheStrategy = CacheStrategy.NETWORK_FIRST,
        offlineFallbackEnabled = true,
    )
    return RuntimeConfig(
        schemaVersion = 2,
        source = RuntimeConfigSource.LOCAL_BUNDLED,
        environment = EndpointEnvironment.PROD,
        timeoutPolicy = TimeoutPolicyConfig(connectTimeoutMs = 10_000, readTimeoutMs = 15_000, writeTimeoutMs = 15_000),
        retryPolicy = RetryPolicyConfig(maxAttempts = 3, baseDelayMs = 250, maxDelayMs = 1_500, multiplier = 2.0),
        circuitBreaker = CircuitBreakerConfig(failureThreshold = 4, resetTimeoutMs = 8_000, gracefulDegradationEnabled = true),
        transports = mapOf(
            NetworkFeature.FEED to defaultTransport.copy(cacheStrategy = CacheStrategy.STALE_WHILE_REVALIDATE),
            NetworkFeature.COMMENTS to defaultTransport,
            NetworkFeature.CHATS to defaultTransport,
            NetworkFeature.RECOMMENDATIONS to defaultTransport.copy(cacheStrategy = CacheStrategy.CACHE_FIRST),
            NetworkFeature.MEDIA_PREFETCH to defaultTransport.copy(cacheStrategy = CacheStrategy.CACHE_FIRST),
            NetworkFeature.CHANNELS to defaultTransport,
            NetworkFeature.ACTIVITY to defaultTransport.copy(cacheStrategy = CacheStrategy.STALE_WHILE_REVALIDATE),
            NetworkFeature.PROFILE to defaultTransport.copy(cacheStrategy = CacheStrategy.CACHE_FIRST),
        ),
        experiments = mapOf(
            "motion_profile" to "full",
            "power_saving" to "disabled",
            "battery_percent" to "100",
        ),
        remoteOverrides = mapOf(
            FeatureFlagKey.AUTOPLAY to true,
            FeatureFlagKey.RECOMMENDED_FEED to true,
        ),
        fetchedAt = timeProvider.now(),
    )
}
