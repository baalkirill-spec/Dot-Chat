package com.streamgram.data.fake

import com.streamgram.core.common.IdGenerator
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.network.EndpointConfig
import com.streamgram.core.network.EndpointEnvironment
import com.streamgram.core.network.EndpointResolver
import com.streamgram.core.network.NetworkClient
import com.streamgram.core.network.NetworkFeature
import com.streamgram.core.network.NetworkRequest
import com.streamgram.core.network.NetworkResponse
import com.streamgram.core.network.NetworkResult
import com.streamgram.core.network.TraceContext
import com.streamgram.core.network.TransportKind
import com.streamgram.core.network.policy.CircuitBreaker
import com.streamgram.core.network.policy.CircuitBreakerState
import com.streamgram.core.network.policy.NetworkPolicy
import com.streamgram.core.network.policy.RetryPolicy
import com.streamgram.core.network.policy.TransportSelector
import com.streamgram.core.runtimeconfig.RuntimeConfigRepository
import com.streamgram.core.telemetry.FailureReason
import com.streamgram.core.telemetry.RequestTrace
import com.streamgram.core.telemetry.StatusBucket
import com.streamgram.core.telemetry.TelemetryLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeEndpointResolver @Inject constructor() : EndpointResolver {
    override suspend fun resolve(
        feature: NetworkFeature,
        environment: EndpointEnvironment,
    ): EndpointConfig {
        val baseUrl = when (environment) {
            EndpointEnvironment.DEV -> "https://dev.api.streamgram.local"
            EndpointEnvironment.STAGE -> "https://stage.api.streamgram.local"
            EndpointEnvironment.PROD -> "https://api.streamgram.local"
        }
        return EndpointConfig(
            environment = environment,
            baseUrl = baseUrl,
            allowlistedHosts = setOf(
                "api.streamgram.local",
                "stage.api.streamgram.local",
                "dev.api.streamgram.local",
                "media.streamgram.local",
            ),
            connectTimeoutMs = 10_000,
            readTimeoutMs = 15_000,
        )
    }
}

@Singleton
class FakeNetworkClient @Inject constructor() : NetworkClient {
    override suspend fun execute(request: NetworkRequest): NetworkResult {
        delay(40)
        return NetworkResult.Success(
            NetworkResponse(
                requestId = request.traceContext.requestId,
                statusCode = 200,
                body = """{"ok":true,"feature":"${request.feature.name.lowercase()}"}""",
                headers = mapOf("x-streamgram-source" to "fake"),
                transportKind = TransportKind.FAKE,
            ),
        )
    }
}

@Singleton
class AppOnlyTransportGateway @Inject constructor(
    private val networkClient: NetworkClient,
    private val endpointResolver: EndpointResolver,
    private val networkPolicy: NetworkPolicy,
    private val transportSelector: TransportSelector,
    private val retryPolicy: RetryPolicy,
    private val circuitBreaker: CircuitBreaker,
    private val runtimeConfigRepository: RuntimeConfigRepository,
    private val telemetryLogger: TelemetryLogger,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) {
    suspend fun execute(feature: NetworkFeature, path: String): NetworkResult {
        val runtimeConfig = runtimeConfigRepository.current()
        val endpoint = endpointResolver.resolve(feature = feature, environment = runtimeConfig.environment)
        val requestId = idGenerator.newId(prefix = "req")
        val startedAt = timeProvider.now()
        val request = NetworkRequest(
            feature = feature,
            path = path,
            traceContext = TraceContext(requestId = requestId, startedAt = startedAt),
        )

        when (val state = circuitBreaker.state(feature)) {
            is CircuitBreakerState.Open -> {
                val failure = NetworkResult.Failure(requestId = requestId, message = state.reason)
                telemetryLogger.logRequest(
                    RequestTrace(
                        requestId = requestId,
                        feature = feature.name,
                        endpointHost = endpoint.baseUrl,
                        latencyMs = 0,
                        statusBucket = StatusBucket.POLICY_BLOCKED,
                        retryCount = 0,
                        failureReason = FailureReason("circuit_open", state.reason),
                        occurredAt = timeProvider.now(),
                    ),
                )
                return failure
            }

            else -> Unit
        }

        val policyDecision = networkPolicy.evaluate(request = request, endpointConfig = endpoint)
        if (!policyDecision.allowed) {
            return NetworkResult.Failure(
                requestId = requestId,
                message = policyDecision.reason.orEmpty(),
            ).also {
                telemetryLogger.logRequest(
                    RequestTrace(
                        requestId = requestId,
                        feature = feature.name,
                        endpointHost = endpoint.baseUrl,
                        latencyMs = 0,
                        statusBucket = StatusBucket.POLICY_BLOCKED,
                        retryCount = 0,
                        failureReason = FailureReason("policy_blocked", policyDecision.reason),
                        occurredAt = timeProvider.now(),
                    ),
                )
            }
        }

        transportSelector.select(feature = feature, config = runtimeConfig)

        var attempt = 1
        while (true) {
            val result = networkClient.execute(request)
            val latencyMs = timeProvider.now().toEpochMilli() - startedAt.toEpochMilli()
            when (result) {
                is NetworkResult.Success -> {
                    circuitBreaker.onSuccess(feature)
                    telemetryLogger.logRequest(
                        RequestTrace(
                            requestId = requestId,
                            feature = feature.name,
                            endpointHost = endpoint.baseUrl,
                            latencyMs = latencyMs,
                            statusBucket = StatusBucket.SUCCESS,
                            retryCount = attempt - 1,
                            occurredAt = timeProvider.now(),
                        ),
                    )
                    return result
                }

                is NetworkResult.Failure -> {
                    circuitBreaker.onFailure(feature)
                    telemetryLogger.logRequest(
                        RequestTrace(
                            requestId = requestId,
                            feature = feature.name,
                            endpointHost = endpoint.baseUrl,
                            latencyMs = latencyMs,
                            statusBucket = StatusBucket.UNKNOWN,
                            retryCount = attempt - 1,
                            failureReason = FailureReason("network_failure", result.message),
                            occurredAt = timeProvider.now(),
                        ),
                    )
                    val delayMs = retryPolicy.nextDelayMillis(feature = feature, attempt = attempt)
                    if (delayMs == null) return result
                    delay(delayMs)
                    attempt += 1
                }
            }
        }
    }
}
