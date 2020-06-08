package com.smqpro.zetnews.view.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.Resource
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.view.NewsApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class HomeViewModel(
    app: Application,
    private val repository: HomeRepository
) : AndroidViewModel(app) {
    val cachedNews = MutableLiveData<Resource<List<Result>>>()
    val news = MutableLiveData<Resource<List<Result>>>()
    var searchPage = 1
    var query = ""
    var section: String? = null
    val loadedNews = mutableListOf<Result>()

    var filter = 0

    init {
        fetchCachedNews()
    }

    fun fetchCachedNews() = viewModelScope.launch {
        cachedNews.postValue(Resource.Loading())
        val resultList = Resource.Success(repository.getCachedNews())
        if (resultList.data?.size == 0) {
            cachedNews.postValue(Resource.Error("No cached news"))
        } else {
            cachedNews.postValue(resultList)
        }
    }

    fun cacheNews(resultList: List<Result>) = viewModelScope.launch {
        Log.d(TAG, "cacheNews: upsert list size - ${resultList.size}")
        repository.upsertCachedNews(resultList)
    }

    fun searchNews() = viewModelScope.launch {
        searchPage = 1
        news.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {
                val order = when (filter) {
                    0 -> Constants.ORDER.NEWEST
                    1 -> Constants.ORDER.OLDEST
                    2 -> Constants.ORDER.RELEVANCE
                    else -> Constants.ORDER.NEWEST
                }
                val response =
                    repository.getNews(
                        searchQuery = query,
                        newsPage = searchPage,
                        order = order,
                        category = section
                    )
                news.postValue(handleNewsResponse(response))
            } else {
                news.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(Resource.Error("Failed to connect to the server"))
                else -> news.postValue(Resource.Error("Error fetching the data"))
            }
        }

    }

    fun likeLikeNot(result: Result) = viewModelScope.launch {
        repository.likeLikeNot(result)
    }

    fun updateNews(resultList: List<Result>) = viewModelScope.launch {
        repository.upsertCachedNews(resultList)
    }

    private fun handleNewsResponse(response: Response<News>): Resource<List<Result>> {
        if (response.isSuccessful) {
            response.body()?.let { resResponse ->
                return Resource.Success(resResponse.response.results)
            }
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
            false
        }
    }

}