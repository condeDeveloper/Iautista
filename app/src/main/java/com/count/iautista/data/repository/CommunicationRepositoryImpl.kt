package com.count.iautista.data.repository

import com.count.iautista.data.local.dao.CommunicationCategoryDao
import com.count.iautista.data.local.dao.CommunicationItemDao
import com.count.iautista.data.local.dao.ProfileItemUsageDao
import com.count.iautista.data.local.database.toDomain
import com.count.iautista.data.local.database.toEntity
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.domain.repository.CommunicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunicationRepositoryImpl @Inject constructor(
    private val categoryDao: CommunicationCategoryDao,
    private val itemDao: CommunicationItemDao,
    private val usageDao: ProfileItemUsageDao,
) : CommunicationRepository {

    override fun getCategories(): Flow<List<CommunicationCategory>> =
        categoryDao.getAllCategories().map { it.map { e -> e.toDomain() } }

    override fun getItemsByCategory(categoryId: Long): Flow<List<CommunicationItem>> =
        itemDao.getItemsByCategory(categoryId).map { it.map { e -> e.toDomain() } }

    override fun getFavoriteItems(): Flow<List<CommunicationItem>> =
        itemDao.getFavoriteItems().map { it.map { e -> e.toDomain() } }

    override fun getMostUsedItems(profileId: Long, limit: Int): Flow<List<CommunicationItem>> =
        usageDao.getMostUsedItems(profileId, limit).map { it.map { e -> e.toDomain() } }

    override fun getItemsByTexts(texts: List<String>): Flow<List<CommunicationItem>> =
        itemDao.getItemsByTexts(texts).map { it.map { e -> e.toDomain() } }

    override suspend fun getItemByText(text: String): CommunicationItem? =
        itemDao.getItemByText(text)?.toDomain()

    override suspend fun saveItem(item: CommunicationItem): Long =
        itemDao.insert(item.toEntity())

    override suspend fun updateItem(item: CommunicationItem) =
        itemDao.update(item.toEntity())

    override suspend fun deleteItem(item: CommunicationItem) =
        itemDao.delete(item.toEntity())

    override suspend fun toggleFavorite(itemId: Long, isFavorite: Boolean) =
        itemDao.updateFavorite(itemId, isFavorite)

    override suspend fun incrementUsage(profileId: Long, itemId: Long) =
        usageDao.incrementUsage(profileId, itemId)

    override suspend fun countCustomItems(): Int =
        itemDao.countCustomItems()
}
