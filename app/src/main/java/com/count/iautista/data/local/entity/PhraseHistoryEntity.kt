package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrase_history")
data class PhraseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phraseText: String,
    val itemIds: List<Long> = emptyList(), // serializado via Converters.kt
    val createdAt: Long = System.currentTimeMillis(),
    val hourOfDay: Int = 0,
    val appMode: String = "CASA",          // AppMode.name — padrão CASA para registros antigos
)
