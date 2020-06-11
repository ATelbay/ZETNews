package com.smqpro.zetnews.view.liked

import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.Result

class LikedRepository(private val db: NewsDatabase) {
    fun getLikedNews() = db.getNewsDao().selectLiked()

    suspend fun upsertNews(result: Result) = db.getNewsDao().upsert(result)

}