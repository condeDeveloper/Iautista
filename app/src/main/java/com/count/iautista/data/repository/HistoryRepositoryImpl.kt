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
     * Remove entradas com mais de 30 dias (gerenciamento de storage).
     * O gate de exibição de 7 dias para usuários free é feito na UI.
     */
    override suspend fun cleanup() {
        phraseHistoryDao.deleteOlderThan(daysAgo(30))
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
