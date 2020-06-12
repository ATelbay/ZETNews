package com.smqpro.zetnews.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smqpro.zetnews.model.response.Tag
import org.jetbrains.annotations.TestOnly
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromTagList(tags: MutableList<Tag>?): String? {
        tags?.let {
            val type = object : TypeToken<MutableList<Tag>?>() {}.type
            return gson.toJson(it, type)
        }
        return null
    }


    @TypeConverter
    fun toTagList(tagsString: String?): MutableList<Tag>? {
        tagsString?.let {
            val type = object : TypeToken<MutableList<Tag>?>() {}.type
            return gson.fromJson<MutableList<Tag>>(it, type)
        }
        return null
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}

