package com.smqpro.zetnews.view.detail

import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.Result

class DetailsRepository(private val db: NewsDatabase) {
    suspend fun deleteNews(result: Result) = db.getNewsDao().deleteResult(result)

    suspend fun upsert(result: Result) = db.getNewsDao().upsert(result)
}
