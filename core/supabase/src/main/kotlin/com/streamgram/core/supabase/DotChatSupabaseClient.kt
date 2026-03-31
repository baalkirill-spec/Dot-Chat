package com.streamgram.core.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DotChatSupabaseClient @Inject constructor(
    val config: DotChatSupabaseConfig,
) {
    val isConfigured: Boolean
        get() = config.url.isNotBlank() && config.anonKey.isNotBlank()

    val client: SupabaseClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey,
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(Functions)
        }
    }

    val auth: Auth
        get() = client.pluginManager.getPlugin(Auth.Companion)

    val postgrest: Postgrest
        get() = client.pluginManager.getPlugin(Postgrest.Companion)

    val storage: Storage
        get() = client.pluginManager.getPlugin(Storage.Companion)

    val realtime: Realtime
        get() = client.pluginManager.getPlugin(Realtime.Companion)

    val functions: Functions
        get() = client.pluginManager.getPlugin(Functions.Companion)
}
