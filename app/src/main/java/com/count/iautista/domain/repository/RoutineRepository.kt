package com.count.iautista.domain.repository

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {

    fun getAllRoutineItems(profileId: Long): Flow<List<RoutineItem>>

    suspend fun saveItem(item: RoutineItem): Long

    suspend fun updateItem(item: RoutineItem)

    suspend fun deleteItem(item: RoutineItem)

    suspend fun updateStatus(itemId: Long, status: RoutineStatus)

    suspend fun getAllItemsOnce(profileId: Long): List<RoutineItem>

    suspend fun resetDailyRoutine(profileId: Long)

    suspend fun resetWithSchedule(profileId: Long, currentHour: Int)

    suspend fun markCurrentAsDone(profileId: Long)

    suspend fun seedDefaultRoutine(profileId: Long)
}
