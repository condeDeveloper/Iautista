package com.count.iautista.data.local.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromLongList(value: List<Long>?): String =
        value?.joinToString(",") ?: ""

    @TypeConverter
    fun toLongList(value: String?): List<Long> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").mapNotNull { it.trim().toLongOrNull() }
}
