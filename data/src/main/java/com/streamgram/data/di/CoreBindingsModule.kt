package com.streamgram.data.di

import com.streamgram.core.common.AppDispatchers
import com.streamgram.core.common.DefaultAppDispatchers
import com.streamgram.core.common.IdGenerator
import com.streamgram.core.common.SystemTimeProvider
import com.streamgram.core.common.TimeProvider
import com.streamgram.core.common.UuidIdGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindingsModule {
    @Binds
    abstract fun bindAppDispatchers(impl: DefaultAppDispatchers): AppDispatchers

    @Binds
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider
}
