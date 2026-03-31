package com.streamgram.core.model

data class DeviceSession(
    val id: String,
    val deviceName: String,
    val platform: String,
    val ipCountryHint: String?,
    val isCurrent: Boolean,
    val lastActiveAtEpochSeconds: Long,
    val signedInAtEpochSeconds: Long,
)

data class SecurityNotification(
    val id: String,
    val title: String,
    val body: String,
    val createdAtEpochSeconds: Long,
)
