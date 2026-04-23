package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.RoutineItemDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.data.local.entity.RoutineItemEntity
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

    override fun getAllRoutineItems(profileId: Long): Flow<List<RoutineItem>> =
        routineItemDao.getAllRoutineItems(profileId).map { it.map { e -> e.toDomain() } }

    override suspend fun saveItem(item: RoutineItem): Long =
        routineItemDao.insert(item.toEntity())

    override suspend fun updateItem(item: RoutineItem) =
        routineItemDao.update(item.toEntity())

    override suspend fun deleteItem(item: RoutineItem) =
        routineItemDao.delete(item.toEntity())

    override suspend fun updateStatus(itemId: Long, status: RoutineStatus) =
        routineItemDao.updateStatus(itemId, status.name)

    override suspend fun getAllItemsOnce(profileId: Long): List<RoutineItem> =
        routineItemDao.getAllOnce(profileId).map { it.toDomain() }

    override suspend fun resetDailyRoutine(profileId: Long) =
        routineItemDao.resetDailyRoutine(profileId)

    override suspend fun resetWithSchedule(profileId: Long, currentHour: Int) {
        val items = routineItemDao.getAllOnce(profileId).sortedBy { it.order }
        routineItemDao.resetDailyRoutine(profileId)
        if (items.isEmpty()) return
        val nowIndex = items
            .indexOfLast { (it.suggestedHour ?: 0) <= currentHour }
            .let { if (it < 0) 0 else it }
        routineItemDao.updateStatus(items[nowIndex].id, "NOW")
        if (nowIndex + 1 < items.size) {
            routineItemDao.updateStatus(items[nowIndex + 1].id, "NEXT")
        }
    }

    override suspend fun markCurrentAsDone(profileId: Long) =
        routineItemDao.markCurrentAsDone(profileId)

    override suspend fun seedDefaultRoutine(profileId: Long) {
        if (routineItemDao.count(profileId) > 0) return
        routineItemDao.insertAll(listOf(
            rot(profileId, "Acordar",           "☀️", "DONE",  0, 7),
            rot(profileId, "Escovar os dentes", "🦷", "DONE",  1, 7),
            rot(profileId, "Café da manhã",     "🥐", "NOW",   2, 8),
            rot(profileId, "Escola",            "🎒", "NEXT",  3, 9),
            rot(profileId, "Terapia",           "🌟", "LATER", 4, 11),
            rot(profileId, "Almoço",            "🍽️","LATER", 5, 12),
            rot(profileId, "Brincar",           "🧸", "LATER", 6, 14),
            rot(profileId, "Banho",             "🛁", "LATER", 7, 17),
            rot(profileId, "Jantar",            "🍽️","LATER", 8, 18),
            rot(profileId, "Dormir",            "😴", "LATER", 9, 21),
        ))
    }

    private fun rot(profileId: Long, text: String, emoji: String, status: String, order: Int, hour: Int) =
        RoutineItemEntity(profileId = profileId, text = text, emoji = emoji,
            status = status, order = order, suggestedHour = hour)
}
