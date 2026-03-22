package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.CommunicationRepository
import com.count.iautista.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Registra o uso de um item único:
 * - incrementa o contador de uso
 * - salva no histórico de frases
 */
class TrackItemUsageUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val historyRepository: HistoryRepository,
) {
    suspend operator fun invoke(item: CommunicationItem) {
        communicationRepository.incrementUsage(item.id)
        historyRepository.savePhrase(
            PhraseHistory(
                phraseText = item.text,
                itemIds    = listOf(item.id),
            )
        )
    }
}
