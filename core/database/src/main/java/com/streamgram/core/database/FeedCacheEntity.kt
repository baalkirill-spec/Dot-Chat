package com.streamgram.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_cache")
data class FeedCacheEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val payload: String,
    val score: Double,
    val origin: String,
    val cachedAtEpochMs: Long,
)
