package com.count.iautista.data.repository

import com.count.iautista.data.billing.BillingService
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

    override fun getRecentPhrases(limit: Int): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getRecentPhrases(limit).map { list -> list.map { it.toDomain() } }

    override fun getPhrasesForToday(): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getPhrasesAfter(startOfToday()).map { list -> list.map { it.toDomain() } }

    override fun getPhrasesForWeek(): Flow<List<PhraseHistory>> =
        phraseHistoryDao.getPhrasesAfter(daysAgo(7)).map { list -> list.map { it.toDomain() } }

    override suspend fun savePhrase(phrase: PhraseHistory) {
        phraseHistoryDao.insert(phrase.toEntity())
    }

    /**
     * Remove entradas com mais de FREE_HISTORY_DAYS dias.
     * Alinhado com BillingService.FREE_HISTORY_DAYS.
     */
    override suspend fun cleanup() {
        phraseHistoryDao.deleteOlderThan(daysAgo(BillingService.FREE_HISTORY_DAYS.toLong()))
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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
