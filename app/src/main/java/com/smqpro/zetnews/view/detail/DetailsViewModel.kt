package com.smqpro.zetnews.view.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smqpro.zetnews.model.response.Result
import kotlinx.coroutines.launch
import java.util.*

class DetailsViewModel(private val repository: DetailsRepository) : ViewModel() {

    fun upsertNews(result: Result) = viewModelScope.launch {
        result.updatedAt = Date()
        repository.upsert(result)
    }
}