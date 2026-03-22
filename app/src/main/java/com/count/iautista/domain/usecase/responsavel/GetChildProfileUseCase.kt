package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    /** Flow reativo — use em ViewModels que observam o perfil continuamente. */
    operator fun invoke(): Flow<ChildProfile?> = repository.getProfile()

    /** Snapshot pontual — use no onboarding ou em operações de escrita. */
    suspend fun once(): ChildProfile? = repository.getProfileOnce()
}
