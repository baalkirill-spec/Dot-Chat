package com.streamgram.feature.activity

import com.streamgram.core.model.ActivityNotification

data class ActivityUiState(
    val items: List<ActivityNotification> = emptyList(),
)
