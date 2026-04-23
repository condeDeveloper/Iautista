package com.count.iautista.domain.usecase.historico

import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

class SaveQuickPhraseUseCase @Inject constructor(
    private val repository: HistoryRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(text: String, mode: AppMode = AppMode.CASA) {
        if (text.isBlank()) return
        val profileId = profileRepository.getProfileOnce()?.id ?: 0L
        repository.savePhrase(
            PhraseHistory(profileId = profileId, phraseText = text.trim(), appMode = mode)
        )
    }
}
