package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

class DeleteRoutineItemUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(item: RoutineItem) = repository.deleteItem(item)
}
