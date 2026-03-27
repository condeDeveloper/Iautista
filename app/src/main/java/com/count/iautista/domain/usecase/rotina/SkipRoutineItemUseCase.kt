package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

/**
 * Pula o item atual (NOW) mandando-o para o final da fila (LATER com order máximo).
 * Promove a cadeia: NEXT → NOW, primeiro LATER restante → NEXT.
 */
class SkipRoutineItemUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(skippedItem: RoutineItem) {
        // 1. Snapshot atual para calcular o maior order
        val items = repository.getAllItemsOnce()
        val maxOrder = items.maxOfOrNull { it.order } ?: skippedItem.order

        // 2. Move o item pulado para o final da fila como LATER
        repository.updateItem(
            skippedItem.copy(
                status = RoutineStatus.LATER,
                order  = maxOrder + 1,
            )
        )

        // 3. Snapshot fresco após a atualização
        val updated = repository.getAllItemsOnce()

        // 4. Promove NEXT → NOW (se não houver NEXT, promove o primeiro LATER → NOW)
        val nextItem = updated.firstOrNull { it.status == RoutineStatus.NEXT }
        val otherLater = updated
            .filter { it.status == RoutineStatus.LATER && it.id != skippedItem.id }
            .sortedBy { it.order }

        if (nextItem != null) {
            repository.updateItem(nextItem.copy(status = RoutineStatus.NOW))
            // 5. Primeiro LATER restante → NEXT
            otherLater.firstOrNull()?.let { repository.updateItem(it.copy(status = RoutineStatus.NEXT)) }
        } else {
            // Sem NEXT: primeiro LATER vai direto pra NOW, segundo pra NEXT
            otherLater.getOrNull(0)?.let { repository.updateItem(it.copy(status = RoutineStatus.NOW)) }
            otherLater.getOrNull(1)?.let { repository.updateItem(it.copy(status = RoutineStatus.NEXT)) }
        }
    }
}
