package com.count.iautista.domain.repository

import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {

    fun getAllRoutineItems(): Flow<List<RoutineItem>>

    suspend fun saveItem(item: RoutineItem): Long

    suspend fun updateItem(item: RoutineItem)

    suspend fun deleteItem(item: RoutineItem)

    suspend fun updateStatus(itemId: Long, status: RoutineStatus)

    /** Volta todos os itens ativos (NOW, NEXT, LATER) para LATER */
    suspend fun resetDailyRoutine()

    /** Marca o item atual (NOW) como DONE */
    suspend fun markCurrentAsDone()
}
