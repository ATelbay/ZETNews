package com.smqpro.zetnews.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_page")
data class CurrentPage(
    @PrimaryKey
    var id: Long,
    var currentPage: Int = 0
)