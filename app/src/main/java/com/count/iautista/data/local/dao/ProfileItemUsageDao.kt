package com.count.iautista.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.count.iautista.data.local.entity.CommunicationItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileItemUsageDao {

    @Query("""
        SELECT ci.* FROM communication_items ci
        INNER JOIN profile_item_usage piu ON ci.id = piu.itemId
        WHERE piu.profileId = :profileId
        ORDER BY piu.usageCount DESC
        LIMIT :limit
    """)
    fun getMostUsedItems(profileId: Long, limit: Int): Flow<List<CommunicationItemEntity>>

    @Query("INSERT OR IGNORE INTO profile_item_usage (profileId, itemId, usageCount) VALUES (:profileId, :itemId, 0)")
    suspend fun insertIfMissing(profileId: Long, itemId: Long)

    @Query("UPDATE profile_item_usage SET usageCount = usageCount + 1 WHERE profileId = :profileId AND itemId = :itemId")
    suspend fun incrementCount(profileId: Long, itemId: Long)

    @Transaction
    suspend fun incrementUsage(profileId: Long, itemId: Long) {
        insertIfMissing(profileId, itemId)
        incrementCount(profileId, itemId)
    }
}
