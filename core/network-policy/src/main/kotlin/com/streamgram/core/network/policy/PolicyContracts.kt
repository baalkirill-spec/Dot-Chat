package com.streamgram.core.network.policy

import com.streamgram.core.network.EndpointConfig
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.network.NetworkRequest
import com.streamgram.core.runtimeconfig.RuntimeConfig

data class PolicyDecision(
    val allowed: Boolean,
    val reason: String? = null,
)

data class TransportSelection(
    val transportKind: String,
    val backendStrategy: String,
    val cacheStrategy: String,
    val offlineFallbackEnabled: Boolean,
)

sealed interface CircuitBreakerState {
    data object Closed : CircuitBreakerState
    data class Open(val reason: String) : CircuitBreakerState
    data object HalfOpen : CircuitBreakerState
}

interface NetworkPolicy {
    fun evaluate(request: NetworkRequest, endpointConfig: EndpointConfig): PolicyDecision
}

interface TransportSelector {
    fun select(feature: NetworkFeature, config: RuntimeConfig): TransportSelection
}

interface RetryPolicy {
    fun nextDelayMillis(feature: NetworkFeature, attempt: Int): Long?
}

interface CircuitBreaker {
    fun state(feature: NetworkFeature): CircuitBreakerState
    fun onSuccess(feature: NetworkFeature)
    fun onFailure(feature: NetworkFeature)
}
