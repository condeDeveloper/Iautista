package com.count.iautista.domain.usecase.historico

import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

enum class HistoryFilter { RECENT, TODAY, WEEK }

class GetPhrasesHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke(filter: HistoryFilter): Flow<List<PhraseHistory>> = when (filter) {
        HistoryFilter.RECENT -> repository.getRecentPhrases(30)
        HistoryFilter.TODAY  -> repository.getPhrasesForToday()
        HistoryFilter.WEEK   -> repository.getPhrasesForWeek()
    }
}
