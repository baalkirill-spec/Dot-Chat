package com.streamgram.core.network

import java.time.Instant

enum class EndpointEnvironment {
    DEV,
    STAGE,
    PROD,
}

enum class NetworkFeature {
    FEED,
    COMMENTS,
    CHATS,
    RECOMMENDATIONS,
    MEDIA_PREFETCH,
    CHANNELS,
    ACTIVITY,
    PROFILE,
}

enum class HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
}

enum class CacheStrategy {
    CACHE_FIRST,
    NETWORK_FIRST,
    CACHE_ONLY,
    NETWORK_ONLY,
    STALE_WHILE_REVALIDATE,
}

enum class TransportKind {
    FAKE,
    MOCK,
    REAL,
    CACHE_ONLY,
}

enum class BackendStrategy {
    FAKE_BACKEND,
    MOCK_BACKEND,
    FIRST_PARTY_API,
    AGGREGATOR_API,
}

data class EndpointConfig(
    val environment: EndpointEnvironment,
    val baseUrl: String,
    val allowlistedHosts: Set<String>,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
)

data class TraceContext(
    val requestId: String,
    val startedAt: Instant,
)

data class NetworkRequest(
    val feature: NetworkFeature,
    val path: String,
    val method: HttpMethod = HttpMethod.GET,
    val query: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val cacheStrategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val traceContext: TraceContext,
)

data class NetworkResponse(
    val requestId: String,
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>,
    val transportKind: TransportKind,
)

sealed interface NetworkResult {
    data class Success(val response: NetworkResponse) : NetworkResult

    data class Failure(
        val requestId: String,
        val code: Int? = null,
        val message: String,
    ) : NetworkResult
}

interface NetworkClient {
    suspend fun execute(request: NetworkRequest): NetworkResult
}

interface EndpointResolver {
    suspend fun resolve(
        feature: NetworkFeature,
        environment: EndpointEnvironment,
    ): EndpointConfig
}
