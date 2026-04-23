package com.count.iautista.domain.usecase.rotina

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.RoutineRepository
import javax.inject.Inject

/**
 * Marca um item como DONE e avança a cadeia automaticamente:
 *   DONE → NEXT vira NOW → primeiro LATER vira NEXT
 *
 * Isso elimina a necessidade de o responsável promover itens manualmente.
 */
class AdvanceRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(doneItem: RoutineItem) {
        // 1. Marca o item atual como DONE
        repository.updateItem(
            doneItem.copy(
                status      = RoutineStatus.DONE,
                completedAt = System.currentTimeMillis(),
            )
        )
        // 2. Snapshot fresco após a atualização
        val items = repository.getAllItemsOnce(doneItem.profileId)

        // 3. Promove NEXT → NOW (se não houver NEXT, promove o primeiro LATER → NOW)
        val nextItem = items.firstOrNull { it.status == RoutineStatus.NEXT }
        val laterItems = items.filter { it.status == RoutineStatus.LATER }.sortedBy { it.order }

        if (nextItem != null) {
            repository.updateItem(nextItem.copy(status = RoutineStatus.NOW))
            // 4. Promove o primeiro LATER → NEXT
            laterItems.firstOrNull()?.let { repository.updateItem(it.copy(status = RoutineStatus.NEXT)) }
        } else {
            // Não há NEXT: primeiro LATER vai direto pra NOW, segundo pra NEXT
            laterItems.getOrNull(0)?.let { repository.updateItem(it.copy(status = RoutineStatus.NOW)) }
            laterItems.getOrNull(1)?.let { repository.updateItem(it.copy(status = RoutineStatus.NEXT)) }
        }
    }
}
