package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profile LIMIT 1")
    fun getProfile(): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profile LIMIT 1")
    suspend fun getProfileOnce(): ChildProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ChildProfileEntity): Long

    @Update
    suspend fun update(profile: ChildProfileEntity)

    @Query("DELETE FROM child_profile")
    suspend fun deleteAll()
}
