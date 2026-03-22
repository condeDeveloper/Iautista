package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineGroup
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Retorna a rotina já agrupada por status.
 * A UI consome um RoutineGroup em vez de filtrar listas diretamente.
 */
class GetRoutineItemsUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    operator fun invoke(): Flow<RoutineGroup> =
        repository.getAllRoutineItems().map { items ->
            RoutineGroup(
                now   = items.filter { it.status == RoutineStatus.NOW },
                next  = items.filter { it.status == RoutineStatus.NEXT },
                later = items.filter { it.status == RoutineStatus.LATER },
                done  = items.filter { it.status == RoutineStatus.DONE },
            )
        }
}
