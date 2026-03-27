package com.count.iautista.domain.repository

import com.count.iautista.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /** Flow reativo do perfil ativo */
    fun getProfile(): Flow<ChildProfile?>

    /** Snapshot pontual — útil no onboarding antes de observar o Flow */
    suspend fun getProfileOnce(): ChildProfile?

    /** Flow reativo de todos os perfis ordenados por data de criação */
    fun getAllProfiles(): Flow<List<ChildProfile>>

    /** ID do perfil ativo (0 = nenhum selecionado → usa o primeiro disponível) */
    fun getActiveProfileId(): Flow<Long>

    suspend fun saveProfile(profile: ChildProfile): Long

    suspend fun updateProfile(profile: ChildProfile)

    suspend fun deleteProfile(profile: ChildProfile)

    suspend fun setActiveProfileId(id: Long)
}
