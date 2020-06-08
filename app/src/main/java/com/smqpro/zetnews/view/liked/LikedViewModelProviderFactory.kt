package com.smqpro.zetnews.view.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class LikedViewModelProviderFactory(private val repository: LikedRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LikedViewModel(repository) as T
    }

}