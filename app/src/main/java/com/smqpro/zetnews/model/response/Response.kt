package com.smqpro.zetnews.model.response

data class Response(
    val currentPage: Int,
    val orderBy: String,
    val pageSize: Int,
    val pages: Int,
    var results: List<Result>,
    val startIndex: Int,
    val status: String,
    val total: Int,
    val userTier: String
)