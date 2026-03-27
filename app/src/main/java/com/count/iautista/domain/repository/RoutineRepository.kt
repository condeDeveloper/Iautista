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

    /** Snapshot único de todos os itens, ordenados por `order` */
    suspend fun getAllItemsOnce(): List<RoutineItem>

    /** Volta todos os itens ativos (NOW, NEXT, LATER) para LATER */
    suspend fun resetDailyRoutine()

    /**
     * Reset inteligente: reseta tudo para LATER e calcula NOW/NEXT
     * com base no [currentHour] e no campo suggestedHour de cada item.
     */
    suspend fun resetWithSchedule(currentHour: Int)

    /** Marca o item atual (NOW) como DONE */
    suspend fun markCurrentAsDone()
}
