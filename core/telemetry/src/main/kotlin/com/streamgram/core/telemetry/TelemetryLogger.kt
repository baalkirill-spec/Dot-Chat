package com.streamgram.core.telemetry

interface TelemetryLogger {
    suspend fun log(event: TelemetryEvent)
    suspend fun logRequest(trace: RequestTrace)
}
