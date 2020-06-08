package com.smqpro.zetnews.view.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import com.smqpro.zetnews.util.*
import com.smqpro.zetnews.util.Constants.Companion.SEARCH_DELAY
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
        initRecycler()
        initRefreshLayout()
        val homeRepository = HomeRepository((activity as MainActivity).db)
        val application = (activity as MainActivity).application
        viewModel =
            ViewModelProvider(this, HomeViewModelProviderFactory(application, homeRepository))
                .get(HomeViewModel::class.java)
        setHasOptionsMenu(true)
        observeCachedNews()
        Log.d(TAG, "onViewCreated: $tag")

    }

    fun scrollToTop() = home_recycler.smoothScrollToPosition(0)

    private fun observeCachedNews() {
        viewModel.cachedNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    observeNews()
                    Log.d(TAG, "observeCachedNews: Success - $response. Results - ${response.data}")
                    if (response.data != null) {
                        viewModel.loadedNews.addAll(response.data)
                        homeAdapter.submitList(response.data)
                    } else {
                        Log.d(TAG, "observeNews: Loading...")
                        home_progress.visibility = View.GONE
                        home_srl.isRefreshing = false
                        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show()
                    }

                }
                is Resource.Error -> { // TODO handle error cases
                    Log.e(
                        TAG,
                        "observeNews: Error - ${response.message}"
                    )
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    home_progress.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun observeNews() {
        viewModel.searchNews()
        viewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "observeNews: Success - $response. Results - ${response.data}")
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    response.data?.let { news ->
                        CoroutineScope(Dispatchers.Default).launch {
                            if (news != viewModel.loadedNews as List<Result>) {
                                viewModel.cacheNews(news)
                                viewModel.loadedNews.addAll(news)
                                withContext(Dispatchers.Main) {
                                    homeAdapter.submitList(news)
                                }
                            } else {
                                news.forEach {
                                    it.cache = true
                                }
                            }
                        }
                        Log.d(TAG, "observeNews: array size - ${news.size}")
                    }
                }
                is Resource.Error -> { // TODO handle error cases
                    if (viewModel.loadedNews.isEmpty()) {
                        Toast.makeText(context, "Error. Try again later", Toast.LENGTH_SHORT).show()
                    }
                    logE(TAG, response.message)
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                }
                is Resource.Loading -> {
                    home_progress.visibility = View.VISIBLE
                    Log.d(TAG, "observeNews: Loading...")
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
            viewModel.loadedNews.clear()
            viewModel.searchNews()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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