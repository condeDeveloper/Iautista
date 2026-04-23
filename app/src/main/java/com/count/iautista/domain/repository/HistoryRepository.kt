package com.count.iautista.domain.repository

import com.count.iautista.domain.model.PhraseHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun getRecentPhrases(profileId: Long, limit: Int): Flow<List<PhraseHistory>>

    fun getPhrasesForToday(profileId: Long): Flow<List<PhraseHistory>>

    fun getPhrasesForWeek(profileId: Long): Flow<List<PhraseHistory>>

    suspend fun savePhrase(phrase: PhraseHistory)

    suspend fun cleanup(profileId: Long)
}
