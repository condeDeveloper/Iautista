package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class SaveChildProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val routineRepository: RoutineRepository,
) {
    suspend operator fun invoke(profile: ChildProfile): Long {
        return if (profile.id != 0L) {
            repository.updateProfile(profile)
            profile.id
        } else {
            val newId = repository.saveProfile(profile)
            routineRepository.seedDefaultRoutine(newId)
            newId
        }
    }
}
