package com.streamgram.core.common

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface AppDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultAppDispatchers @Inject constructor() : AppDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}

interface TimeProvider {
    fun now(): Instant
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): Instant = Instant.now()
}

interface IdGenerator {
    fun newId(prefix: String? = null): String
}

class UuidIdGenerator @Inject constructor() : IdGenerator {
    override fun newId(prefix: String?): String {
        val raw = UUID.randomUUID().toString()
        return prefix?.let { "$it-$raw" } ?: raw
    }
}
