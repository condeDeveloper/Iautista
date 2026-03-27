package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.ChildProfileDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val childProfileDao: ChildProfileDao,
    private val dataStore: UserPreferencesDataStore,
) : ProfileRepository {

    override fun getProfile(): Flow<ChildProfile?> = combine(
        childProfileDao.getAllProfiles(),
        dataStore.activeChildProfileId,
    ) { profiles, activeId ->
        val active = if (activeId != 0L) profiles.find { it.id == activeId } else null
        active?.toDomain() ?: profiles.firstOrNull()?.toDomain()
    }

    override suspend fun getProfileOnce(): ChildProfile? =
        childProfileDao.getProfileOnce()?.toDomain()

    override fun getAllProfiles(): Flow<List<ChildProfile>> =
        childProfileDao.getAllProfiles().map { list -> list.map { it.toDomain() } }

    override fun getActiveProfileId(): Flow<Long> = dataStore.activeChildProfileId

    override suspend fun saveProfile(profile: ChildProfile): Long =
        childProfileDao.insert(profile.toEntity())

    override suspend fun updateProfile(profile: ChildProfile) {
        childProfileDao.update(profile.toEntity())
    }

    override suspend fun deleteProfile(profile: ChildProfile) {
        childProfileDao.delete(profile.toEntity())
    }

    override suspend fun setActiveProfileId(id: Long) {
        dataStore.setActiveChildProfileId(id)
    }
}
