package com.count.iautista.domain.usecase.historico

import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Salva uma frase rápida (sem itens de comunicação associados).
 * Usado pelos atalhos da tela Início.
 */
class SaveQuickPhraseUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(text: String) {
        if (text.isBlank()) return
        repository.savePhrase(PhraseHistory(phraseText = text.trim()))
    }
}
