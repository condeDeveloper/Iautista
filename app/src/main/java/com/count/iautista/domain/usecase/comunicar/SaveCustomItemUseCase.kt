package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.data.billing.BillingService
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import javax.inject.Inject

sealed class SaveItemResult {
    data class Success(val itemId: Long) : SaveItemResult()
    object PremiumLimitReached : SaveItemResult()
}

/**
 * Salva um item customizado criado pelo responsável.
 * Verifica o limite de itens gratuitos antes de prosseguir.
 */
class SaveCustomItemUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val billingService: BillingService,
) {
    suspend operator fun invoke(item: CommunicationItem): SaveItemResult {
        val currentCount = communicationRepository.countCustomItems()

        if (!billingService.canAddCustomItem(currentCount)) {
            return SaveItemResult.PremiumLimitReached
        }

        val id = communicationRepository.saveItem(item.copy(isDefault = false))
        return SaveItemResult.Success(id)
    }
}
