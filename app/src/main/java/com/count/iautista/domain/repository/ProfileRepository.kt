package com.count.iautista.domain.repository

import com.count.iautista.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /** Flow reativo do perfil ativo (único perfil na v1) */
    fun getProfile(): Flow<ChildProfile?>

    /** Snapshot pontual — útil no onboarding antes de observar o Flow */
    suspend fun getProfileOnce(): ChildProfile?

    suspend fun saveProfile(profile: ChildProfile)

    suspend fun updateProfile(profile: ChildProfile)
}
