package ru.netology.nework.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.UserPreview

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLongSet(value: Set<Long>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongSet(value: String?): Set<Long>? {
        if (value == null) return null
        val setType = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(value, setType)
    }

    @TypeConverter
    fun fromUserPreviewMap(value: Map<Long, UserPreview>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toUserPreviewMap(value: String?): Map<Long, UserPreview>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<Long, UserPreview>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
