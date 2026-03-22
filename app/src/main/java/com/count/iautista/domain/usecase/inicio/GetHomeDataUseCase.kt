package com.count.iautista.domain.usecase.inicio

import com.count.iautista.domain.model.HomeData
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.CommunicationRepository
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val historyRepository: HistoryRepository,
    private val communicationRepository: CommunicationRepository,
    private val routineRepository: RoutineRepository,
) {
    operator fun invoke(): Flow<HomeData> = combine(
        profileRepository.getProfile(),
        historyRepository.getRecentPhrases(10),
        communicationRepository.getMostUsedItems(8),
        routineRepository.getAllRoutineItems(),
    ) { profile, recentPhrases, mostUsed, routineItems ->
        HomeData(
            profile = profile,
            greeting = buildGreeting(profile?.name),
            recentPhrases = recentPhrases,
            mostUsedItems = mostUsed,
            routineNow  = routineItems.filter { it.status == RoutineStatus.NOW },
            routineNext = routineItems.filter { it.status == RoutineStatus.NEXT },
        )
    }

    private fun buildGreeting(name: String?): String {
        val period = when (LocalTime.now().hour) {
            in 0..11  -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else      -> "Boa noite"
        }
        return if (name.isNullOrBlank()) "$period!" else "$period, $name!"
    }
}
