package com.smqpro.zetnews.view.home

import android.util.Log
import com.smqpro.zetnews.model.RetrofitInstance
import com.smqpro.zetnews.model.db.NewsDatabase
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.TAG
import retrofit2.Response

class HomeRepository(
    val db: NewsDatabase
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
//        response.body()?.response?.results?.filter {
//            Log.d(
//                TAG,
//                "getNews: ${it.fields.thumbnail.isNotEmpty() && it.fields.trailText.isNotEmpty()}"
//            )
//            it.fields.thumbnail.isNotEmpty() && it.fields.trailText.isNotEmpty() // TODO doesn't filter any of the news without thumbnail & trailText
//        }
        Log.d(TAG, "getNews: size - ${response.body()?.response?.results?.size}")
        return response
    }
}