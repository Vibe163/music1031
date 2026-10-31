package com.localmusic.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.localmusic.app.data.model.FingerprintCacheEntity

/** 音频指纹缓存访问。 */
@Dao
interface FingerprintCacheDao {

    @Query("SELECT * FROM fingerprint_cache")
    suspend fun getAll(): List<FingerprintCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<FingerprintCacheEntity>)
}