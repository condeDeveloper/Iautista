package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    suspend operator fun invoke(item: CommunicationItem) {
        repository.toggleFavorite(item.id, !item.isFavorite)
    }
}
