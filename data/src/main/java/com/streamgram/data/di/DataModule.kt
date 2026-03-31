package com.streamgram.data.di

import com.streamgram.core.featureflags.FeatureFlagRepository
import com.streamgram.core.livekit.LiveKitRoomController
import com.streamgram.core.livekit.LiveKitCloudConfig
import com.streamgram.core.livekit.LiveKitRoomControllerImpl
import com.streamgram.core.livekit.LiveKitCloudTokenProvider
import com.streamgram.core.livekit.LiveKitTokenProvider
import com.streamgram.core.network.EndpointResolver
import com.streamgram.core.network.NetworkClient
import com.streamgram.core.network.policy.AllowlistNetworkPolicy
import com.streamgram.core.network.policy.CircuitBreaker
import com.streamgram.core.network.policy.ConfigBackedTransportSelector
import com.streamgram.core.network.policy.ExponentialBackoffRetryPolicy
import com.streamgram.core.network.policy.InMemoryCircuitBreaker
import com.streamgram.core.network.policy.NetworkPolicy
import com.streamgram.core.network.policy.RetryPolicy
import com.streamgram.core.network.policy.TransportSelector
import com.streamgram.core.runtimeconfig.RuntimeConfigRepository
import com.streamgram.core.telemetry.TelemetryLogger
import com.streamgram.data.fake.FakeEndpointResolver
import com.streamgram.data.fake.FakeNetworkClient
import com.streamgram.data.repository.LiveKitCallsRepository
import com.streamgram.data.repository.AndroidTelemetryLogger
import com.streamgram.data.repository.PreferencesFeatureFlagRepository
import com.streamgram.data.repository.PreferencesRuntimeConfigRepository
import com.streamgram.data.repository.PreferencesSettingsRepository
import com.streamgram.data.repository.SupabaseActivityRepository
import com.streamgram.data.repository.SupabaseAuthRepository
import com.streamgram.data.repository.SupabaseChannelsRepository
import com.streamgram.data.repository.SupabaseChatsRepository
import com.streamgram.data.repository.SupabaseCommentsRepository
import com.streamgram.data.repository.SupabaseContactsRepository
import com.streamgram.data.repository.SupabasePrivacyRepository
import com.streamgram.data.repository.SupabaseSessionsRepository
import com.streamgram.domain.repository.ActivityRepository
import com.streamgram.domain.repository.AuthRepository
import com.streamgram.domain.repository.CallsRepository
import com.streamgram.domain.repository.ChannelsRepository
import com.streamgram.domain.repository.ChatsRepository
import com.streamgram.domain.repository.CommentsRepository
import com.streamgram.domain.repository.ContactsRepository
import com.streamgram.domain.repository.PrivacyRepository
import com.streamgram.domain.repository.SessionsRepository
import com.streamgram.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {
    @Binds
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    abstract fun bindChannelsRepository(impl: SupabaseChannelsRepository): ChannelsRepository

    @Binds
    abstract fun bindCommentsRepository(impl: SupabaseCommentsRepository): CommentsRepository

    @Binds
    abstract fun bindChatsRepository(impl: SupabaseChatsRepository): ChatsRepository

    @Binds
    abstract fun bindContactsRepository(impl: SupabaseContactsRepository): ContactsRepository

    @Binds
    abstract fun bindActivityRepository(impl: SupabaseActivityRepository): ActivityRepository

    @Binds
    abstract fun bindSettingsRepository(impl: PreferencesSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindPrivacyRepository(impl: SupabasePrivacyRepository): PrivacyRepository

    @Binds
    abstract fun bindSessionsRepository(impl: SupabaseSessionsRepository): SessionsRepository

    @Binds
    abstract fun bindCallsRepository(impl: LiveKitCallsRepository): CallsRepository

    @Binds
    abstract fun bindFeatureFlagRepository(impl: PreferencesFeatureFlagRepository): FeatureFlagRepository

    @Binds
    abstract fun bindRuntimeConfigRepository(impl: PreferencesRuntimeConfigRepository): RuntimeConfigRepository

    @Binds
    abstract fun bindTelemetryLogger(impl: AndroidTelemetryLogger): TelemetryLogger

    // TODO: Replace with real NetworkClient and EndpointResolver when transport layer is implemented
    @Binds
    abstract fun bindNetworkClient(impl: FakeNetworkClient): NetworkClient

    @Binds
    abstract fun bindEndpointResolver(impl: FakeEndpointResolver): EndpointResolver
}

@Module
@InstallIn(SingletonComponent::class)
object TransportPolicyModule {
    @Provides
    @Singleton
    fun provideNetworkPolicy(): NetworkPolicy = AllowlistNetworkPolicy()

    @Provides
    @Singleton
    fun provideTransportSelector(): TransportSelector = ConfigBackedTransportSelector()

    @Provides
    @Singleton
    fun provideRetryPolicy(runtimeConfigRepository: PreferencesRuntimeConfigRepository): RetryPolicy {
        return ExponentialBackoffRetryPolicy(configProvider = runtimeConfigRepository::snapshot)
    }

    @Provides
    @Singleton
    fun provideCircuitBreaker(runtimeConfigRepository: PreferencesRuntimeConfigRepository): CircuitBreaker {
        return InMemoryCircuitBreaker(
            failureThreshold = runtimeConfigRepository.snapshot().circuitBreaker.failureThreshold,
        )
    }

    @Provides
    @Singleton
    fun provideLiveKitTokenProvider(
        config: LiveKitCloudConfig,
    ): LiveKitTokenProvider = LiveKitCloudTokenProvider(config)

    @Provides
    @Singleton
    fun provideLiveKitRoomController(
        @ApplicationContext context: Context,
        config: LiveKitCloudConfig,
        tokenProvider: LiveKitTokenProvider,
    ): LiveKitRoomController {
        return LiveKitRoomControllerImpl(
            appContext = context,
            config = config,
            tokenProvider = tokenProvider,
        )
    }
}
