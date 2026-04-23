package com.count.iautista.domain.repository

import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import kotlinx.coroutines.flow.Flow

interface CommunicationRepository {

    fun getCategories(): Flow<List<CommunicationCategory>>

    fun getItemsByCategory(categoryId: Long): Flow<List<CommunicationItem>>

    fun getFavoriteItems(): Flow<List<CommunicationItem>>

    fun getMostUsedItems(profileId: Long, limit: Int): Flow<List<CommunicationItem>>

    fun getItemsByTexts(texts: List<String>): Flow<List<CommunicationItem>>

    suspend fun getItemByText(text: String): CommunicationItem?

    suspend fun saveItem(item: CommunicationItem): Long

    suspend fun updateItem(item: CommunicationItem)

    suspend fun deleteItem(item: CommunicationItem)

    suspend fun toggleFavorite(itemId: Long, isFavorite: Boolean)

    suspend fun incrementUsage(profileId: Long, itemId: Long)

    suspend fun countCustomItems(): Int
}
