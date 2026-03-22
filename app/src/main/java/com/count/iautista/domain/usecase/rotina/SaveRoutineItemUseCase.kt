package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class SaveRoutineItemUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(item: RoutineItem): Long = repository.saveItem(item)
}
