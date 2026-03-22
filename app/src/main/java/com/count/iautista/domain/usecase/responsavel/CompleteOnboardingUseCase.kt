package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Finaliza o onboarding: salva o perfil da criança e marca o onboarding como concluído.
 * Chamado na última tela do fluxo de configuração inicial.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dataStore: UserPreferencesDataStore,
) {
    suspend operator fun invoke(profile: ChildProfile) {
        profileRepository.saveProfile(profile)
        dataStore.setOnboardingCompleted(true)
    }
}
