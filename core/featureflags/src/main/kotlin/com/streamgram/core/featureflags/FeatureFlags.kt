package com.streamgram.core.featureflags

import kotlinx.coroutines.flow.Flow

enum class FeatureFlagKey {
    AUTOPLAY,
    COMMENTS,
    LIKE_DISLIKE,
    EMOJI_REACTIONS,
    RECOMMENDED_FEED,
    FOLLOWS,
}

data class FeatureFlag(
    val key: FeatureFlagKey,
    val enabled: Boolean,
    val source: FlagSource,
)

enum class FlagSource {
    LOCAL_DEFAULT,
    LOCAL_OVERRIDE,
    REMOTE_CONFIG,
}

interface FeatureFlagRepository {
    fun observeFlags(): Flow<Map<FeatureFlagKey, FeatureFlag>>
    fun observeFlag(key: FeatureFlagKey): Flow<FeatureFlag>
    suspend fun refresh()
}
