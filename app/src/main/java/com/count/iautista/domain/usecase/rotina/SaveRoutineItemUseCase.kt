package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class SaveRoutineItemUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(item: RoutineItem): Long {
        val itemWithProfile = if (item.profileId == 0L) {
            val profileId = profileRepository.getProfileOnce()?.id ?: 0L
            item.copy(profileId = profileId)
        } else {
            item
        }
        return repository.saveItem(itemWithProfile)
    }
}
