package com.streamgram.domain.repository

import com.streamgram.core.model.ActivityNotification
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivity(): Flow<List<ActivityNotification>>
}
