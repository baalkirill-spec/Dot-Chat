package com.streamgram.domain.repository

import com.streamgram.core.model.RecommendationProfile
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun observeProfile(): Flow<RecommendationProfile>
    suspend fun refreshProfile()
}
