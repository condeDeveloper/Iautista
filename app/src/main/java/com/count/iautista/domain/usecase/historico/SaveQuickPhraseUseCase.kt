package com.count.iautista.domain.usecase.historico

import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Salva uma frase rápida (sem itens de comunicação associados).
 * Usado pelos atalhos da tela Início e sugestões contextuais do Comunicar.
 */
class SaveQuickPhraseUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(text: String, mode: AppMode = AppMode.CASA) {
        if (text.isBlank()) return
        repository.savePhrase(PhraseHistory(phraseText = text.trim(), appMode = mode))
    }
}
