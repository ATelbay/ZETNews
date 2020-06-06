package com.smqpro.zetnews.view.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smqpro.zetnews.model.response.News
import com.smqpro.zetnews.util.Constants
import com.smqpro.zetnews.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    val news = MutableLiveData<Resource<News>>()
    var newsPage = 1
    var searchPage = 1
    var query = ""
    var section: Constants.SECTIONS? = null

    var filter = 0

    init {
        getNews()
    }


    fun getNews() = viewModelScope.launch {
        news.postValue(Resource.Loading())
        val response = homeRepository.getNews(newsPage)
        news.postValue(handleNewsResponse(response))
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
            homeRepository.getNews(
                searchQuery = query,
                newsPage = searchPage,
                order = order,
                category = section
            )
        news.postValue(handleNewsResponse(response))
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