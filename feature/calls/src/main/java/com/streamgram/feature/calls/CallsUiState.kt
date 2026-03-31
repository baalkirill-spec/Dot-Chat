package com.streamgram.feature.calls

import com.streamgram.core.model.CallSession
import com.streamgram.core.model.CallType

data class CallsUiState(
    val activeCall: CallSession? = null,
    val requestedType: CallType = CallType.AUDIO,
    val canRetryJoin: Boolean = false,
    val isLoading: Boolean = false,
    val errorKey: String? = null,
)
