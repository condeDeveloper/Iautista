package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.ChildProfileDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val childProfileDao: ChildProfileDao,
) : ProfileRepository {

    override fun getProfile(): Flow<ChildProfile?> =
        childProfileDao.getProfile().map { it?.toDomain() }

    override suspend fun getProfileOnce(): ChildProfile? =
        childProfileDao.getProfileOnce()?.toDomain()

    override suspend fun saveProfile(profile: ChildProfile) {
        childProfileDao.insert(profile.toEntity())
    }

    override suspend fun updateProfile(profile: ChildProfile) {
        childProfileDao.update(profile.toEntity())
    }
}
