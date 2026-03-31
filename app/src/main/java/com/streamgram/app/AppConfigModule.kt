package com.streamgram.app

import com.streamgram.core.livekit.LiveKitCloudConfig
import com.streamgram.core.supabase.DotChatSupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    fun provideSupabaseConfig(): DotChatSupabaseConfig {
        return DotChatSupabaseConfig(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY,
        )
    }

    @Provides
    @Singleton
    fun provideLiveKitCloudConfig(): LiveKitCloudConfig {
        return LiveKitCloudConfig(
            url = BuildConfig.LIVEKIT_URL,
            tokenEndpoint = BuildConfig.LIVEKIT_TOKEN_ENDPOINT,
        )
    }
}
