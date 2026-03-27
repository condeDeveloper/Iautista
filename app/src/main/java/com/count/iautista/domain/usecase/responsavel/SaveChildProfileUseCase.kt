package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Upsert do perfil da criança.
 * Se já existir (id != 0), faz update; caso contrário, insere e retorna o novo ID.
 */
class SaveChildProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(profile: ChildProfile): Long {
        return if (profile.id != 0L) {
            repository.updateProfile(profile)
            profile.id
        } else {
            repository.saveProfile(profile)
        }
    }
}
