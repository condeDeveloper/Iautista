package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineGroup
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRoutineItemsUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(): Flow<RoutineGroup> =
        profileRepository.getProfile().flatMapLatest { profile ->
            val profileId = profile?.id ?: 0L
            repository.getAllRoutineItems(profileId).map { items ->
                RoutineGroup(
                    now   = items.filter { it.status == RoutineStatus.NOW },
                    next  = items.filter { it.status == RoutineStatus.NEXT },
                    later = items.filter { it.status == RoutineStatus.LATER },
                    done  = items.filter { it.status == RoutineStatus.DONE },
                )
            }
        }
}
