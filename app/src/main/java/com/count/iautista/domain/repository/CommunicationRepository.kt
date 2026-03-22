package com.count.iautista.domain.repository

import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import kotlinx.coroutines.flow.Flow

interface CommunicationRepository {

    // ── Leitura ──────────────────────────────────────────────────────────────

    fun getCategories(): Flow<List<CommunicationCategory>>

    fun getItemsByCategory(categoryId: Long): Flow<List<CommunicationItem>>

    fun getFavoriteItems(): Flow<List<CommunicationItem>>

    /** limit sem default — evita problema de parâmetro default em Room */
    fun getMostUsedItems(limit: Int): Flow<List<CommunicationItem>>

    // ── Escrita ──────────────────────────────────────────────────────────────

    suspend fun saveItem(item: CommunicationItem): Long

    suspend fun updateItem(item: CommunicationItem)

    suspend fun deleteItem(item: CommunicationItem)

    suspend fun toggleFavorite(itemId: Long, isFavorite: Boolean)

    suspend fun incrementUsage(itemId: Long)

    // ── Contagem / utilitários ────────────────────────────────────────────────

    /** Conta apenas itens criados pelo responsável (não padrão) */
    suspend fun countCustomItems(): Int
}
