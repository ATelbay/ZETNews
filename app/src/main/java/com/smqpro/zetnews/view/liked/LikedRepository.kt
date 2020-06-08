package com.smqpro.zetnews.view.liked

import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.Result

class LikedRepository(val db: NewsDatabase) {
    fun getLikedNews() = db.getNewsDao().selectLiked()

    suspend fun deleteNews(result: Result) = db.getNewsDao().deleteResult(result)

    suspend fun saveNews(result: Result) = db.getNewsDao().upsert(result)
}