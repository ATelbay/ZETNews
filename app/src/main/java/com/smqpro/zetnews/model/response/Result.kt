package com.smqpro.zetnews.model.response

import androidx.room.*
import com.smqpro.zetnews.model.db.Converters
import java.io.Serializable

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
    var webUrl: String
) : Serializable {
    constructor() : this(
        "", "", false, "", "",
        "", "", Field("", ""),
        "", "", "", ""
    )

    @TypeConverters(Converters::class)
    var tags: MutableList<Tag> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Result

        if (apiUrl != other.apiUrl ||
            pillarId != other.pillarName ||
            sectionName != other.sectionName ||
            fields.thumbnail != other.fields.thumbnail ||
            fields.trailText != other.fields.trailText ||
            webPublicationDate != other.webPublicationDate ||
            webTitle != other.webTitle ||
            webUrl != other.webUrl
        ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = apiUrl.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + isHosted.hashCode()
        result = 31 * result + pillarId.hashCode()
        result = 31 * result + pillarName.hashCode()
        result = 31 * result + sectionId.hashCode()
        result = 31 * result + sectionName.hashCode()
        result = 31 * result + fields.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + webPublicationDate.hashCode()
        result = 31 * result + webTitle.hashCode()
        result = 31 * result + webUrl.hashCode()
        result = 31 * result + tags.hashCode()
        return result
    }
}