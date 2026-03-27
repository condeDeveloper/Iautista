package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.CommunicationItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationItemDao {

    @Query("SELECT * FROM communication_items WHERE categoryId = :categoryId ORDER BY `order` ASC")
    fun getItemsByCategory(categoryId: Long): Flow<List<CommunicationItemEntity>>

    @Query("SELECT * FROM communication_items WHERE isFavorite = 1 ORDER BY usageCount DESC")
    fun getFavoriteItems(): Flow<List<CommunicationItemEntity>>

    // limit fixo para evitar problemas com parâmetros default em Room
    @Query("SELECT * FROM communication_items ORDER BY usageCount DESC LIMIT 8")
    fun getTop8MostUsed(): Flow<List<CommunicationItemEntity>>

    @Query("SELECT * FROM communication_items ORDER BY usageCount DESC LIMIT :limit")
    fun getMostUsedItems(limit: Int): Flow<List<CommunicationItemEntity>>

    @Query("SELECT * FROM communication_items WHERE id = :id")
    suspend fun getItemById(id: Long): CommunicationItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CommunicationItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CommunicationItemEntity): Long

    @Update
    suspend fun update(item: CommunicationItemEntity)

    @Delete
    suspend fun delete(item: CommunicationItemEntity)

    @Query("UPDATE communication_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE communication_items SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("SELECT COUNT(*) FROM communication_items WHERE isDefault = 0")
    suspend fun countCustomItems(): Int

    /** Snapshot único de itens customizados — usado para sync com Firestore */
    @Query("SELECT * FROM communication_items WHERE isDefault = 0 ORDER BY createdAt ASC")
    suspend fun getAllCustomItemsOnce(): List<CommunicationItemEntity>

    /** Snapshot único de todos os itens (default + custom) com favorites — usado para sync de favoritos */
    @Query("SELECT * FROM communication_items WHERE isFavorite = 1")
    suspend fun getAllFavoritesOnce(): List<CommunicationItemEntity>
}
