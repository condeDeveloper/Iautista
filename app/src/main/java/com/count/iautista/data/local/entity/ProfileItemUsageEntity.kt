package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "profile_item_usage",
    primaryKeys = ["profileId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CommunicationItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index("itemId")],
)
data class ProfileItemUsageEntity(
    val profileId: Long,
    val itemId: Long,
    val usageCount: Int = 0,
)
