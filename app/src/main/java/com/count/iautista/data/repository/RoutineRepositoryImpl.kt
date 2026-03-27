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

    override suspend fun getAllItemsOnce(): List<RoutineItem> =
        routineItemDao.getAllOnce().map { it.toDomain() }

    override suspend fun resetDailyRoutine() =
        routineItemDao.resetDailyRoutine()

    override suspend fun resetWithSchedule(currentHour: Int) {
        val items = routineItemDao.getAllOnce().sortedBy { it.order }
        // 1. Reseta todos os ativos para LATER
        routineItemDao.resetDailyRoutine()
        if (items.isEmpty()) return
        // 2. Encontra o item cuja hora sugerida é a mais próxima sem ultrapassar currentHour.
        //    Se nenhum item tem suggestedHour <= currentHour, usa o primeiro por ordem.
        val nowIndex = items
            .indexOfLast { (it.suggestedHour ?: 0) <= currentHour }
            .let { if (it < 0) 0 else it }
        // 3. Define NOW
        routineItemDao.updateStatus(items[nowIndex].id, "NOW")
        // 4. Define NEXT (primeiro item após o NOW)
        if (nowIndex + 1 < items.size) {
            routineItemDao.updateStatus(items[nowIndex + 1].id, "NEXT")
        }
    }

    override suspend fun markCurrentAsDone() =
        routineItemDao.markCurrentAsDone()
}
