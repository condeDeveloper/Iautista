package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.CommunicationRepository
import com.count.iautista.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Constrói o texto final de uma frase a partir de uma lista de itens,
 * incrementa o uso de cada um e salva no histórico.
 *
 * Retorna o texto montado para que o ViewModel passe ao TtsManager.
 */
class BuildPhraseUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val historyRepository: HistoryRepository,
) {
    suspend operator fun invoke(items: List<CommunicationItem>): String {
        require(items.isNotEmpty()) { "Lista de itens não pode ser vazia" }

        val phraseText = items.joinToString(" ") { it.text }

        items.forEach { communicationRepository.incrementUsage(it.id) }

        historyRepository.savePhrase(
            PhraseHistory(
                phraseText = phraseText,
                itemIds    = items.map { it.id },
            )
        )

        return phraseText
    }
}
