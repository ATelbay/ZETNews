package com.smqpro.zetnews.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smqpro.zetnews.model.response.Tag

class Converters {

    companion object {

        private val gson = Gson()

        @JvmStatic
        @TypeConverter
        fun fromTagList(tags: MutableList<Tag>?): String? {
            tags?.let {
                val type = object : TypeToken<MutableList<Tag>?>() {}.type
                return gson.toJson(it, type)
            }
            return null
        }

        @JvmStatic
        @TypeConverter
        fun toTagList(tagsString: String?): MutableList<Tag>? {
            tagsString?.let {
                val type = object : TypeToken<MutableList<Tag>?>() {}.type
                return gson.fromJson<MutableList<Tag>>(it, type)
            }
            return null
        }

    }



}