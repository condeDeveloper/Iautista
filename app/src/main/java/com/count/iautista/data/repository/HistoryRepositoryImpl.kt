package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.PhraseHistoryDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val phraseHistoryDao: PhraseHistoryDao,
) : HistoryRepository {

    override fun getRecentPhrases(profileId: Long, limit: Int): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getRecentPhrases(profileId, limit).map { list -> list.map { it.toDomain() } }

    override fun getPhrasesForToday(profileId: Long): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getPhrasesAfter(profileId, startOfToday()).map { list -> list.map { it.toDomain() } }

    override fun getPhrasesForWeek(profileId: Long): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getPhrasesAfter(profileId, daysAgo(7)).map { list -> list.map { it.toDomain() } }

    override suspend fun savePhrase(phrase: PhraseHistory) {
        phraseHistoryDao.insert(phrase.toEntity())
    }

    override suspend fun cleanup(profileId: Long) {
        phraseHistoryDao.deleteOlderThan(daysAgo(30))
    }

    private fun startOfToday(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    private fun daysAgo(days: Long): Long = LocalDate.now()
        .minusDays(days)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
