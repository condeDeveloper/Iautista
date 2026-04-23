package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.CommunicationRepository
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

class TrackItemUsageUseCase @Inject constructor(
    private val communicationRepository: CommunicationRepository,
    private val historyRepository: HistoryRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(item: CommunicationItem) {
        val profileId = profileRepository.getProfileOnce()?.id ?: 0L
        communicationRepository.incrementUsage(profileId, item.id)
        historyRepository.savePhrase(
            PhraseHistory(
                profileId  = profileId,
                phraseText = item.text,
                itemIds    = listOf(item.id),
            )
        )
    }
}
