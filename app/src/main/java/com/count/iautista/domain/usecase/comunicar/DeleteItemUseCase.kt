package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    suspend operator fun invoke(item: CommunicationItem) {
        check(!item.isDefault) { "Itens padrão não podem ser removidos pelo usuário" }
        repository.deleteItem(item)
    }
}
