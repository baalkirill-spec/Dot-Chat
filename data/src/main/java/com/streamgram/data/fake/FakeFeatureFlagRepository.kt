package com.streamgram.data.fake

import com.streamgram.core.datastore.UserPreferencesDataSource
import com.streamgram.core.featureflags.FeatureFlag
import com.streamgram.core.featureflags.FeatureFlagKey
import com.streamgram.core.featureflags.FeatureFlagRepository
import com.streamgram.core.featureflags.FlagSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class FakeFeatureFlagRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : FeatureFlagRepository {
    override fun observeFlags(): Flow<Map<FeatureFlagKey, FeatureFlag>> {
        return userPreferencesDataSource.preferences.map { preferences ->
            mapOf(
                FeatureFlagKey.AUTOPLAY to FeatureFlag(FeatureFlagKey.AUTOPLAY, preferences.mediaAutoplayEnabled, FlagSource.LOCAL_DEFAULT),
                FeatureFlagKey.COMMENTS to FeatureFlag(FeatureFlagKey.COMMENTS, true, FlagSource.LOCAL_DEFAULT),
                FeatureFlagKey.LIKE_DISLIKE to FeatureFlag(FeatureFlagKey.LIKE_DISLIKE, true, FlagSource.LOCAL_DEFAULT),
                FeatureFlagKey.EMOJI_REACTIONS to FeatureFlag(FeatureFlagKey.EMOJI_REACTIONS, true, FlagSource.LOCAL_DEFAULT),
                FeatureFlagKey.RECOMMENDED_FEED to FeatureFlag(FeatureFlagKey.RECOMMENDED_FEED, preferences.channelRecommendationsEnabled, FlagSource.LOCAL_DEFAULT),
                FeatureFlagKey.FOLLOWS to FeatureFlag(FeatureFlagKey.FOLLOWS, true, FlagSource.LOCAL_DEFAULT),
            )
        }
    }

    override fun observeFlag(key: FeatureFlagKey): Flow<FeatureFlag> {
        return observeFlags().map { flags -> flags.getValue(key) }
    }

    override suspend fun refresh() = Unit
}
