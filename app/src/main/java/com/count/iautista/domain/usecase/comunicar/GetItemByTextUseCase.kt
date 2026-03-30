package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import javax.inject.Inject

class GetItemByTextUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    suspend operator fun invoke(text: String): CommunicationItem? =
        repository.getItemByText(text)
}
