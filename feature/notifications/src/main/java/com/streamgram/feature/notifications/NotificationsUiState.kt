package com.streamgram.feature.notifications

import com.streamgram.core.model.ActivityNotification

data class NotificationsUiState(
    val items: List<ActivityNotification> = emptyList(),
)
