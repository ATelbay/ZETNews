package com.smqpro.zetnews.model.response

import com.smqpro.zetnews.util.Constants

data class Response(
    val currentPage: Int = 1,
    val orderBy: String = Constants.ORDER.NEWEST.name,
    val pageSize: Int = 10,
    val pages: Int = 1,
    var results: List<Result>,
    val startIndex: Int = 1,
    val status: String = "",
    val total: Int = 10,
    val userTier: String = ""
)