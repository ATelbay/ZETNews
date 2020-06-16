package com.smqpro.zetnews.model.response

import androidx.room.*
import com.smqpro.zetnews.model.db.Converters
import java.io.Serializable
import java.util.*

@Entity(tableName = "news")

data class Result(
    var apiUrl: String,
    @PrimaryKey
    var id: String,
    @Ignore
    val isHosted: Boolean,
    @Ignore
    val pillarId: String,
    var pillarName: String,
    @Ignore
    val sectionId: String,
    var sectionName: String,
    @Embedded(prefix = "field_")
    var fields: Field,
    @Ignore
    val type: String,
    var webPublicationDate: String,
    var webTitle: String,
    var webUrl: String,
    var liked: Boolean,
    var cache: Boolean,
    var timestamp: String
) : Serializable {
    constructor() : this(
        "", "", false, "", "",
        "", "", Field("", ""),
        "", "", "", "",
        false, false, ""
    )

    @TypeConverters(Converters::class)
    var tags: MutableList<Tag> = mutableListOf()

    @TypeConverters(Converters::class)
    var createdAt: Date = Date(System.currentTimeMillis())

    @TypeConverters(Converters::class)
    var updatedAt: Date = Date(System.currentTimeMillis())

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Result

        if (apiUrl != other.apiUrl ||
            id != other.id ||
            pillarName != other.pillarName ||
            sectionName != other.sectionName ||
            webPublicationDate != other.webPublicationDate ||
            webTitle != other.webTitle ||
            webUrl != other.webUrl ||
            liked != other.liked ||
            cache != other.cache
        ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = apiUrl.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + pillarName.hashCode()
        result = 31 * result + sectionName.hashCode()
        result = 31 * result + webPublicationDate.hashCode()
        result = 31 * result + webTitle.hashCode()
        result = 31 * result + webUrl.hashCode()
        result = 31 * result + liked.hashCode()
        result = 31 * result + cache.hashCode()
        return result
    }
}