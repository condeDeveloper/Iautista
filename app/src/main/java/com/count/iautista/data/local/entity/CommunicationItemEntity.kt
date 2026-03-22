package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "communication_items",
    foreignKeys = [ForeignKey(
        entity = CommunicationCategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("categoryId")]
)
data class CommunicationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val text: String,
    val emoji: String = "",
    val imageRes: Int? = null,
    val imageUri: String? = null,
    val audioUri: String? = null,
    val isFavorite: Boolean = false,
    val isDefault: Boolean = true,
    val order: Int = 0,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
