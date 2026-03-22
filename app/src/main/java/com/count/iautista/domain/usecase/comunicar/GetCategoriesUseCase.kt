package com.count.iautista.domain.usecase.comunicar

import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.repository.CommunicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CommunicationRepository,
) {
    operator fun invoke(): Flow<List<CommunicationCategory>> = repository.getCategories()
}
