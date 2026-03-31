package com.streamgram.core.telemetry

import com.streamgram.core.model.WatchEvent
import java.time.Instant

data class RequestTrace(
    val requestId: String,
    val feature: String,
    val endpointHost: String,
    val latencyMs: Long,
    val statusBucket: StatusBucket,
    val retryCount: Int,
    val failureReason: FailureReason? = null,
    val occurredAt: Instant,
)

data class FailureReason(
    val category: String,
    val message: String? = null,
)

enum class StatusBucket {
    SUCCESS,
    CLIENT_ERROR,
    SERVER_ERROR,
    OFFLINE,
    POLICY_BLOCKED,
    UNKNOWN,
}

sealed interface TelemetryEvent {
    val name: String
    val occurredAt: Instant
}

data class FeedWatchTelemetryEvent(
    val watchEvent: WatchEvent,
    override val occurredAt: Instant = watchEvent.occurredAt,
) : TelemetryEvent {
    override val name: String = "feed_watch"
}

data class UiTelemetryEvent(
    override val name: String,
    val properties: Map<String, String>,
    override val occurredAt: Instant,
) : TelemetryEvent
