package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.CommunicationRepository
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

class BuildPhraseUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val historyRepository: HistoryRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(items: List<CommunicationItem>): String {
        require(items.isNotEmpty()) { "Lista de itens não pode ser vazia" }

        val phraseText = items.joinToString(" ") { it.text }
        val profileId = profileRepository.getProfileOnce()?.id ?: 0L

        items.forEach { communicationRepository.incrementUsage(profileId, it.id) }

        historyRepository.savePhrase(
            PhraseHistory(
                profileId  = profileId,
                phraseText = phraseText,
                itemIds    = items.map { it.id },
            )
        )

        return phraseText
    }
}
