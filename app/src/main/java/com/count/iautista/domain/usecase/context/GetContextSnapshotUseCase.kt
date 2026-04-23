package com.count.iautista.domain.usecase.context

import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.ContextSnapshot
import com.count.iautista.domain.model.TimeSlot
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetContextSnapshotUseCase @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
    private val historyRepository: HistoryRepository,
    private val profileRepository: ProfileRepository,
    private val getRoutineItems: GetRoutineItemsUseCase,
) {
    operator fun invoke(): Flow<ContextSnapshot> =
        profileRepository.getProfile().flatMapLatest { profile ->
            val profileId = profile?.id ?: 0L
            combine(
                prefsDataStore.preferences.map { it.appMode },
                getRoutineItems().map { group -> group.now.firstOrNull() },
                historyRepository.getPhrasesForToday(profileId),
            ) { mode, nowActivity, todayPhrases ->

                val currentHour  = LocalTime.now().hour
                val slot         = TimeSlot.fromHour(currentHour)
                val thirtyMinAgo = LocalDateTime.now().minusMinutes(30)

                val recentLabels = todayPhrases
                    .filter { it.createdAt.isAfter(thirtyMinAgo) }
                    .groupBy { it.phraseText }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(LABEL_LIMIT)
                    .map { it.key }

                val topLabelsBySlot = todayPhrases
                    .filter { slot.contains(it.hourOfDay) }
                    .groupBy { it.phraseText }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(LABEL_LIMIT)
                    .map { it.key }

                ContextSnapshot(
                    mode            = mode,
                    timeSlot        = slot,
                    nowActivity     = nowActivity,
                    recentLabels    = recentLabels,
                    topLabelsBySlot = topLabelsBySlot,
                )
            }
        }

    private companion object {
        const val LABEL_LIMIT = 5
    }
}
