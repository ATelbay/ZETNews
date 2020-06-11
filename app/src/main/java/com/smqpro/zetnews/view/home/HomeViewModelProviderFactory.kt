@file:Suppress("UNCHECKED_CAST")

package com.smqpro.zetnews.view.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelProviderFactory(
    private val app: Application,
    private val repository: HomeRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(app, repository) as T
    }

}