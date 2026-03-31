package com.streamgram.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FeedCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StreamGramDatabase : RoomDatabase() {
    abstract fun feedCacheDao(): FeedCacheDao
}
