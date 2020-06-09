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
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class HomeViewModel(
    app: Application,
    private val repository: HomeRepository
) : AndroidViewModel(app) {
    val news = MutableLiveData<Resource<List<Result>>>()
    val newsAvailable = MutableLiveData<Boolean>()
    var searchPage = 1
    var query = ""
    var section: String? = null

    var filter = 0

    init {
        searchNews(true)
        newNewsAvailable()
    }

    private fun newNewsAvailable() = viewModelScope.launch {
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
                if (repository.sameResults(response.body()?.response?.results)) {
                    newsAvailable.postValue(false)
                } else newsAvailable.postValue(true)
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(Resource.Error("Failed to connect to the server"))
                else -> news.postValue(Resource.Error("Error fetching the data"))
            }
        }

    }

    private fun cacheNews(resultList: List<Result>) = viewModelScope.launch {
        Log.d(TAG, "cacheNews: upsert list size - ${resultList.size}")
        resultList.forEach {
            it.cache = true
        }
        repository.upsertCachedNews(resultList)
    }

    fun searchNews(getCachedNews: Boolean) = viewModelScope.launch {
        searchPage = 1
        news.postValue(Resource.Loading())
        if (getCachedNews) {
            Log.d(TAG, "searchNews: getting news from the cache")
            if (repository.getCachedNews().isNotEmpty()) {
                news.postValue(Resource.Success(repository.getCachedNews()))
            } else {
                news.postValue(Resource.Error("No cached data"))
            }
        } else {
            Log.d(TAG, "searchNews: getting news from the network")
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
    }

    fun likeLikeNot(result: Result) = viewModelScope.launch {
        repository.likeLikeNot(result)
    }

    private fun handleNewsResponse(response: Response<News>): Resource<List<Result>> {
        if (response.isSuccessful) {
            response.body()?.let { resResponse ->
                cacheNews(resResponse.response.results)
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