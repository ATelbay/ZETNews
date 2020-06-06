package com.smqpro.zetnews.model.response

import java.io.Serializable

data class Field(
    val trailText: String = "",
    val thumbnail: String = ""
) : Serializable
