package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsByCategoryUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    operator fun invoke(categoryId: Long): Flow<List<CommunicationItem>> =
        repository.getItemsByCategory(categoryId)
}
