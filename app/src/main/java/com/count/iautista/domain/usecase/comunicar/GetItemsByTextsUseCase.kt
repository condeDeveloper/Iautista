package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsByTextsUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    operator fun invoke(texts: List<String>): Flow<List<CommunicationItem>> =
        repository.getItemsByTexts(texts)
}
