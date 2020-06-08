package com.smqpro.zetnews.view.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.smqpro.zetnews.model.RetrofitInstance
import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.TAG
import kotlinx.coroutines.launch
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
        Log.d(TAG, "getNews: size - ${response.body()?.response?.results?.size}")
        return response
    }

    suspend fun upsertCachedNews(resultList: List<Result>) {
        db.getNewsDao().deleteCachedNews()
        db.getNewsDao().upsertCachedNews(resultList)
    }

    fun getCachedNews() = db.getNewsDao().selectCached()

    suspend fun likeLikeNot(result: Result) {
        result.liked = !result.liked
        db.getNewsDao().upsert(result)
    }
}