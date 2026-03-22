package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Upsert do perfil da criança.
 * Se já existir (id != 0), faz update; caso contrário, insere.
 */
class SaveChildProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(profile: ChildProfile) {
        val existing = repository.getProfileOnce()
        if (existing == null) {
            repository.saveProfile(profile)
        } else {
            repository.updateProfile(profile.copy(id = existing.id))
        }
    }
}
