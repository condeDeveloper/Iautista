package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineItemDao {

    @Query("SELECT * FROM routine_items ORDER BY `order` ASC")
    fun getAllRoutineItems(): Flow<List<RoutineItemEntity>>

    @Query("SELECT * FROM routine_items WHERE status = :status ORDER BY `order` ASC")
    fun getByStatus(status: String): Flow<List<RoutineItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RoutineItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RoutineItemEntity): Long

    @Update
    suspend fun update(item: RoutineItemEntity)

    @Delete
    suspend fun delete(item: RoutineItemEntity)

    @Query("UPDATE routine_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    // Reseta todos os itens (incluindo DONE) para LATER — novo dia começa do zero
    @Query("UPDATE routine_items SET status = 'LATER'")
    suspend fun resetDailyRoutine()

    @Query("UPDATE routine_items SET status = 'DONE' WHERE status = 'NOW'")
    suspend fun markCurrentAsDone()

    /** Snapshot único — usado em operações de reordenação e progressão de cadeia */
    @Query("SELECT * FROM routine_items ORDER BY `order` ASC")
    suspend fun getAllOnce(): List<RoutineItemEntity>

    @Query("SELECT COUNT(*) FROM routine_items")
    suspend fun count(): Int
}
