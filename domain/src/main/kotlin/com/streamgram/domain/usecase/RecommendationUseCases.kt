package com.streamgram.domain.usecase

import com.streamgram.core.model.RecommendationProfile
import com.streamgram.domain.repository.RecommendationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRecommendationProfileUseCase @Inject constructor(
    private val repository: RecommendationRepository,
) {
    operator fun invoke(): Flow<RecommendationProfile> = repository.observeProfile()
}

class RefreshRecommendationProfileUseCase @Inject constructor(
    private val repository: RecommendationRepository,
) {
    suspend operator fun invoke() = repository.refreshProfile()
}
