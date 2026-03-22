package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class UpdateRoutineStatusUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(item: RoutineItem, newStatus: RoutineStatus) {
        val completedAt = if (newStatus == RoutineStatus.DONE) System.currentTimeMillis() else null
        repository.updateItem(item.copy(status = newStatus, completedAt = completedAt))
    }
}
