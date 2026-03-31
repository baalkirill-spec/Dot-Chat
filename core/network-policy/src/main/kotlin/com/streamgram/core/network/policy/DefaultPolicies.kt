package com.streamgram.core.network.policy

import com.streamgram.core.network.EndpointConfig
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.network.NetworkRequest
import com.streamgram.core.runtimeconfig.RuntimeConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.pow

class AllowlistNetworkPolicy(
    private val denylistedHosts: Set<String> = emptySet(),
) : NetworkPolicy {
    override fun evaluate(request: NetworkRequest, endpointConfig: EndpointConfig): PolicyDecision {
        val host = endpointConfig.baseUrl.substringAfter("://").substringBefore("/")
        return when {
            host in denylistedHosts -> PolicyDecision(
                allowed = false,
                reason = "Host is denylisted for app-scoped policy.",
            )

            host !in endpointConfig.allowlistedHosts -> PolicyDecision(
                allowed = false,
                reason = "Host is not in app allowlist.",
            )

            else -> PolicyDecision(allowed = true)
        }
    }
}

class ConfigBackedTransportSelector : TransportSelector {
    override fun select(feature: NetworkFeature, config: RuntimeConfig): TransportSelection {
        val transport = config.transports.getValue(feature)
        return TransportSelection(
            transportKind = transport.transportKind.name,
            backendStrategy = transport.backendStrategy.name,
            cacheStrategy = transport.cacheStrategy.name,
            offlineFallbackEnabled = transport.offlineFallbackEnabled,
        )
    }
}

class ExponentialBackoffRetryPolicy(
    private val configProvider: () -> RuntimeConfig,
) : RetryPolicy {
    override fun nextDelayMillis(feature: NetworkFeature, attempt: Int): Long? {
        val config = configProvider().retryPolicy
        if (attempt >= config.maxAttempts) return null
        val computed = config.baseDelayMs * config.multiplier.pow(attempt - 1.0)
        return min(computed.toLong(), config.maxDelayMs)
    }
}

class InMemoryCircuitBreaker(
    private val failureThreshold: Int,
) : CircuitBreaker {
    private val failures = ConcurrentHashMap<NetworkFeature, Int>()

    override fun state(feature: NetworkFeature): CircuitBreakerState {
        val count = failures[feature] ?: 0
        return if (count >= failureThreshold) {
            CircuitBreakerState.Open(reason = "failure_threshold_reached")
        } else {
            CircuitBreakerState.Closed
        }
    }

    override fun onSuccess(feature: NetworkFeature) {
        failures.remove(feature)
    }

    override fun onFailure(feature: NetworkFeature) {
        failures.compute(feature) { _, current -> (current ?: 0) + 1 }
    }
}
