package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class ResetDailyRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke() {
        val profileId = profileRepository.getProfileOnce()?.id ?: return
        repository.resetDailyRoutine(profileId)
    }

    suspend fun withSchedule(currentHour: Int) {
        val profileId = profileRepository.getProfileOnce()?.id ?: return
        repository.resetWithSchedule(profileId, currentHour)
    }
}
