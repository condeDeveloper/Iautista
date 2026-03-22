package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.RoutineItemDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val routineItemDao: RoutineItemDao,
) : RoutineRepository {

    override fun getAllRoutineItems(): Flow<List<RoutineItem>> =
        routineItemDao.getAllRoutineItems().map { it.map { e -> e.toDomain() } }

    override suspend fun saveItem(item: RoutineItem): Long =
        routineItemDao.insert(item.toEntity())

    override suspend fun updateItem(item: RoutineItem) =
        routineItemDao.update(item.toEntity())

    override suspend fun deleteItem(item: RoutineItem) =
        routineItemDao.delete(item.toEntity())

    override suspend fun updateStatus(itemId: Long, status: RoutineStatus) =
        routineItemDao.updateStatus(itemId, status.name)

    override suspend fun resetDailyRoutine() =
        routineItemDao.resetDailyRoutine()

    override suspend fun markCurrentAsDone() =
        routineItemDao.markCurrentAsDone()
}
