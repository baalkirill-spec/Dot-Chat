package com.streamgram.core.tdlib.updates

import com.streamgram.core.tdlib.TdLibLifecycleState
import com.streamgram.core.tdlib.TdLibUpdate
import kotlinx.coroutines.flow.Flow

interface TdLibUpdatesService {
    fun observeLifecycle(): Flow<TdLibLifecycleState>
    fun observeUpdates(): Flow<TdLibUpdate>
    suspend fun start()
    suspend fun stop()
}
