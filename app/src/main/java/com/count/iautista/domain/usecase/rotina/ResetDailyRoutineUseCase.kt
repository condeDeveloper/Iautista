package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

/**
 * Reseta todos os itens ativos para LATER.
 * Chamado pelo responsável ao iniciar um novo dia.
 */
class ResetDailyRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke() = repository.resetDailyRoutine()
}
