package com.streamgram.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedCacheDao {
    @Query("SELECT * FROM feed_cache ORDER BY score DESC, cachedAtEpochMs DESC")
    suspend fun getFeed(): List<FeedCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FeedCacheEntity>)

    @Query("DELETE FROM feed_cache")
    suspend fun clear()
}
