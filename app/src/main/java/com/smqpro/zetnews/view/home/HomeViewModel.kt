package com.smqpro.zetnews.view.home

import androidx.lifecycle.*
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    val news = MutableLiveData<Resource<News>>()
    var searchPage = 1
    var query = ""
    var section: String? = null

    var filter = 0

    init {
        searchNews()
    }

    fun getCachedNews() = repository.getCachedNews()

    fun cacheNews(resultList: List<Result>) = viewModelScope.launch {
        repository.upsertCachedNews(resultList)
    }

    fun searchNews() = viewModelScope.launch {
        searchPage = 1
        news.postValue(Resource.Loading())
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
    }

    fun likeLikeNot(result: Result) = viewModelScope.launch {
        repository.likeLikeNot(result)
    }

    fun updateNews(resultList: List<Result>) = viewModelScope.launch {
       repository.upsertCachedNews(resultList)
    }

    private fun handleNewsResponse(response: Response<News>): Resource<News> {
        if (response.isSuccessful) {
            response.body()?.let { resResponse ->
                return Resource.Success(resResponse)
            }
        }
        return Resource.Error(response.message())
    }

}