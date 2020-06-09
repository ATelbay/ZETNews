package com.smqpro.zetnews.view.home

import android.util.Log
import com.smqpro.zetnews.model.RetrofitInstance
import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.TAG
import retrofit2.Response

class HomeRepository(
    private val db: NewsDatabase
) {

    suspend fun getNews(
        newsPage: Int,
        searchQuery: String? = null,
        order: Constants.ORDER = Constants.ORDER.NEWEST,
        category: String? = null
    ): Response<News> {
        val response = RetrofitInstance.api.getNews(
            page = newsPage,
            query = searchQuery,
            order = order,
            section = category
        )
        response.body()?.response?.results?.forEach {
            it.cache = true
        }
        Log.d(TAG, "getNews: size - ${response.body()?.response?.results?.size}")
        return response
    }

    suspend fun sameResults(resultList: List<Result>): Boolean {
        val cachedNews = db.getNewsDao().selectCached()
        var returnCache = true
        if (cachedNews.isNotEmpty() && resultList.isNotEmpty())
            resultList.forEachIndexed { index, result ->
                if (result != cachedNews[index]) {
                    returnCache = false
                    return@forEachIndexed
                }
            } else returnCache = true
        Log.d(TAG, "sameResults: $returnCache")
        return returnCache
    }

    suspend fun upsertCachedNews(resultList: List<Result>) {
        db.getNewsDao().deleteCachedNews()
        db.getNewsDao().upsertCachedNews(resultList)
    }

    suspend fun getCachedNews() = db.getNewsDao().selectCached()

    suspend fun likeLikeNot(result: Result) {
        result.liked = !result.liked
        db.getNewsDao().upsert(result)
    }
}