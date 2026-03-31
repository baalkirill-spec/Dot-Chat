package com.streamgram.data.fake

import android.util.Log
import com.streamgram.core.telemetry.RequestTrace
import com.streamgram.core.telemetry.TelemetryEvent
import com.streamgram.core.telemetry.TelemetryLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTelemetryLogger @Inject constructor() : TelemetryLogger {
    override suspend fun log(event: TelemetryEvent) {
        Log.d("StreamGramTelemetry", "event=${event.name} at=${event.occurredAt}")
    }

    override suspend fun logRequest(trace: RequestTrace) {
        Log.d(
            "StreamGramNetwork",
            "request=${trace.requestId} feature=${trace.feature} bucket=${trace.statusBucket} retries=${trace.retryCount} latencyMs=${trace.latencyMs}",
        )
    }
}
