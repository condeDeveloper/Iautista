package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.PhraseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseHistoryDao {

    @Query("SELECT * FROM phrase_history ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPhrases(limit: Int): Flow<List<PhraseHistoryEntity>>

    @Query("SELECT * FROM phrase_history WHERE createdAt >= :since ORDER BY createdAt DESC")
    fun getPhrasesAfter(since: Long): Flow<List<PhraseHistoryEntity>>

    @Query("""
        SELECT phraseText, COUNT(*) as count
        FROM phrase_history
        WHERE createdAt >= :since
        GROUP BY phraseText
        ORDER BY count DESC
        LIMIT :limit
    """)
    fun getMostUsedPhrases(since: Long, limit: Int): Flow<List<PhraseUsageResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phrase: PhraseHistoryEntity): Long

    @Query("DELETE FROM phrase_history WHERE createdAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM phrase_history")
    suspend fun count(): Int

    /** Snapshot único das N frases mais recentes — usado para sync com Firestore */
    @Query("SELECT * FROM phrase_history ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentOnce(limit: Int): List<PhraseHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phrases: List<PhraseHistoryEntity>)
}

data class PhraseUsageResult(val phraseText: String, val count: Int)
