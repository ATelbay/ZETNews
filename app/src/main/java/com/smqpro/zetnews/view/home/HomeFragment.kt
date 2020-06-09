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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.Constants.Companion.SEARCH_DELAY
import com.smqpro.zetnews.util.Resource
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.logE
import com.smqpro.zetnews.util.sendShareIntent
import com.smqpro.zetnews.view.MainActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.news_item.view.*
import kotlinx.coroutines.*

class HomeFragment : Fragment(R.layout.fragment_home),
    HomeListAdapter.Interaction {
    private lateinit var homeAdapter: HomeListAdapter
    private lateinit var viewModel: HomeViewModel
    var firstInit = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initRefreshLayout()
        val homeRepository = HomeRepository((activity as MainActivity).db)
        val application = (activity as MainActivity).application
        viewModel =
            ViewModelProvider(
                (activity as MainActivity),
                HomeViewModelProviderFactory(application, homeRepository)
            )
                .get(HomeViewModel::class.java)
        setHasOptionsMenu(true)
        initRefreshButton()
        if (savedInstanceState == null) {
            observeCachedNews()
            observeNewNewsAvailability()
        }

    }

    fun scrollToTop() = home_recycler.smoothScrollToPosition(0)

    private fun observeNewNewsAvailability() {
        viewModel.newsAvailable.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeNewNewsAvailability: $it")
            if (it) {
                showRefreshButton()
            }
        })
    }

    private fun observeCachedNews() {
        viewModel.cachedNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "ocn: Success - $response. Results - ${response.data}")
                    if (response.data != null) {
                        homeAdapter.submitList(response.data)
                    } else {
                        viewModel.searchNews()
                    }
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                }
                is Resource.Error -> { // TODO handle error cases
                    Log.e(
                        TAG,
                        "ocn: Error - ${response.message}"
                    )
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    viewModel.searchNews()

                }
                is Resource.Loading -> {
                    Log.d(TAG, "ocn: Loading...")
                    home_refresh_button.isEnabled = false
                    home_progress.visibility = View.VISIBLE
                }
            }
            observeNews()
        })
    }

    private fun observeNews() {
        viewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "on: Success - $response. Results - ${response.data}")
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    if (response.data != null) {
                        viewModel.cacheNews(response.data)
                        viewModel.newNewsAvailable(response.data)
                        homeAdapter.submitList(response.data)
                    } else {
                        Toast.makeText(
                            context,
                            "Something went wrong. Try again later. observeNews().Success.else",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Error -> { // TODO handle error cases
                    Toast.makeText(
                        context,
                        "Something went wrong. Try again later. observeNews().Error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    logE(TAG, response.message)
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false

                }
                is Resource.Loading -> {
                    home_progress.visibility = View.VISIBLE
                    home_refresh_button.isEnabled = false
                    Log.d(TAG, "on: Loading...")
                }
            }
        })
    }


    private fun initRecycler() {
        homeAdapter = HomeListAdapter(this)
        home_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
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
                viewModel.searchNews()
            }
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
                    viewModel.searchNews()
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
                                viewModel.searchNews()
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

    private fun initRefreshButton(work: () -> Unit = {}) {
        home_refresh_button.setOnClickListener {
            viewModel.searchNews()
            hideRefreshButton()
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

    override fun onLikeSelected(item: Result) {
        Log.d(TAG, "onLikeSelected: clicked")
        viewModel.likeLikeNot(item)
    }

}