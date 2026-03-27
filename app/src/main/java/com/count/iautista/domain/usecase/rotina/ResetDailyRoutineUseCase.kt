package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

/**
 * Reseta a rotina diária.
 *
 * [invoke] — reset simples: volta tudo para LATER (botão ↺ do responsável).
 * [withSchedule] — reset inteligente: recalcula NOW/NEXT com base na hora atual.
 *   Chamado automaticamente ao abrir o app num novo dia.
 */
class ResetDailyRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke() = repository.resetDailyRoutine()

    suspend fun withSchedule(currentHour: Int) = repository.resetWithSchedule(currentHour)
}
