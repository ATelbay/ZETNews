package com.smqpro.zetnews.view.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants.Companion.SEARCH_DELAY
import com.smqpro.zetnews.util.Resource
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.sendShareIntent
import com.smqpro.zetnews.view.MainActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.news_item.view.*
import kotlinx.coroutines.*


class HomeFragment : Fragment(R.layout.fragment_home),
    HomeListAdapter.Interaction {
    private lateinit var homeAdapter: HomeListAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initViewModel()
        setHasOptionsMenu(true)
        initRefreshButton()
        initRecycler()
        observeCachedNews()
        observeUpdatedNews()
    }

    fun scrollToTop() = home_recycler.smoothScrollToPosition(0)

    private fun initViewModel() {
        val repository = HomeRepository((activity as MainActivity).db)
        val application = (activity as MainActivity).application
        viewModel = ViewModelProvider(
            (activity as MainActivity),
            HomeViewModelProviderFactory(application, repository)
        )
            .get(HomeViewModel::class.java)
    }

    private fun observeCachedNews() {
        viewModel.cachedNews.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    home_content_progress.visibility = View.GONE
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    homeAdapter.submitList(it.data ?: listOf())
                    Log.d(TAG, "observeCachedNews: current page - ${viewModel.searchPage}")
                    Log.d(TAG, "observeCachedNews: data size - ${it.data?.size}")
                }
                is Resource.Error -> {
                    Log.d(TAG, "observeCachedNews: ${it.message}")
                }
            }
        })
    }

    private fun observeUpdatedNews() {
        viewModel.news.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Error -> {
                    home_content_progress.visibility = View.GONE
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    Toast.makeText(
                        context,
                        "Something went wrong. Try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "observeUpdatedNews: Error - ${it.message}")
                }
            }
        })
    }

    private fun initRecycler() {
        homeAdapter = HomeListAdapter(this)
        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: triggered. Pages - ${viewModel.pages}")
                    if (viewModel.searchPage <= viewModel.pages) {
                        viewModel.getUpdatedNews(false)
                        home_content_progress.visibility = View.VISIBLE
                    }
                }
            }
        }
        home_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
            addOnScrollListener(onScrollListener)
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    private fun initRefreshLayout() {
        home_srl.setOnRefreshListener {
            CoroutineScope(Dispatchers.Default).launch {
                viewModel.getUpdatedNews()
            }
        }
    }

    private fun initRefreshButton() {
        home_refresh_button.setOnClickListener {
            hideRefreshButton()
            viewModel.getUpdatedNews()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setQuery(viewModel.query, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            var job: Job? = null
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY)
                    query?.let {
                        viewModel.query = it // TODO Hardcode
                    }
                    Log.d(TAG, "onQueryTextChange: ")
                    viewModel.getUpdatedNews()
                }
                return false
            }

        })

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_filter -> {
                item.isEnabled = false
                CoroutineScope(Dispatchers.Default).launch {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Filter by date")
                        .setSingleChoiceItems(
                            R.array.filter_by_date,
                            viewModel.filter
                        ) { dialog, which ->
                            if (viewModel.filter != which) {
                                viewModel.filter = which
                                Log.d(TAG, "onOptionsItemSelected: ")
                                viewModel.getUpdatedNews()
                                dialog.dismiss()
                            }
                        }

                    withContext(Dispatchers.Main) {
                        val alertDialog = builder.create()
                        alertDialog.show()
                        item.isEnabled = true
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(position: Int, item: Result, itemView: View) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.d(TAG, "onItemSelected: item on the $position clicked")
            val author = if (item.tags.isNotEmpty()) item.tags[0].webTitle else ""
            val extras = FragmentNavigatorExtras(
                itemView.news_image to item.fields.thumbnail,
                itemView.news_title to item.webTitle,
                itemView.news_description to item.fields.trailText,
                itemView.news_time to item.webPublicationDate,
                itemView.news_author to author,
                itemView.news_category to item.sectionName
            )
            withContext(Dispatchers.Main) {
                val destination = HomeFragmentDirections.toDetailsFragment(item)
                Log.d("TAG", "${item.fields.thumbnail} ${item.webTitle}")
                findNavController().navigate(destination, extras)
            }
        }

    }


    private fun hideRefreshButton() {
        val transAnim = TranslateAnimation(0F, 0F, 0F, -400F)
        transAnim.duration = 500
        val alphaAnim = AlphaAnimation(1F, 0F)
        alphaAnim.duration = 500
        val animSet = AnimationSet(true)
        animSet.addAnimation(transAnim)
        animSet.addAnimation(alphaAnim)
        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                home_refresh_button.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                home_refresh_button.visibility = View.VISIBLE
            }
        })
        home_refresh_button.startAnimation(animSet)
    }

    private fun showRefreshButton() {
        home_refresh_button.isEnabled = true
        val transAnim = TranslateAnimation(0F, 0F, -400F, 0F)
        transAnim.duration = 500
        val alphaAnim = AlphaAnimation(0F, 1F)
        alphaAnim.duration = 500
        val animSet = AnimationSet(true)
        animSet.addAnimation(transAnim)
        animSet.addAnimation(alphaAnim)
        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                home_refresh_button.visibility = View.VISIBLE
            }
        })
        home_refresh_button.startAnimation(animSet)
    }

    override fun onShareSelected(url: String) {
        context?.sendShareIntent(url)
    }

    override fun onLongItemSelected(item: Result) {
        Log.d(TAG, "onLongItemSelected: clicked")
        val destination = HomeFragmentDirections.toWebFragment(item)

        findNavController().navigate(destination)
    }

}