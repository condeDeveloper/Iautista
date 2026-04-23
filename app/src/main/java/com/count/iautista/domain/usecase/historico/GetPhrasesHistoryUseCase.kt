package com.count.iautista.domain.usecase.historico

import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

enum class HistoryFilter { RECENT, TODAY, WEEK }

class GetPhrasesHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(filter: HistoryFilter): Flow<List<PhraseHistory>> =
        profileRepository.getProfile().flatMapLatest { profile ->
            val profileId = profile?.id ?: 0L
            when (filter) {
                HistoryFilter.RECENT -> repository.getRecentPhrases(profileId, 30)
                HistoryFilter.TODAY  -> repository.getPhrasesForToday(profileId)
                HistoryFilter.WEEK   -> repository.getPhrasesForWeek(profileId)
            }
        }
}
