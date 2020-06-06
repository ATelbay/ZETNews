package com.smqpro.zetnews.model.response

import java.io.Serializable

data class Tag(
    val apiUrl: String,
    val bio: String,
    val bylineImageUrl: String,
    val bylineLargeImageUrl: String,
    val emailAddress: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val twitterHandle: String,
    val type: String,
    val webTitle: String,
    val webUrl: String
) : Serializable