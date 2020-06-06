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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var homeViewModel: HomeViewModel
    val args: HomeFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initRefreshLayout()
        val homeRepository = HomeRepository((activity as MainActivity).db)
        homeViewModel = ViewModelProvider(this, HomeViewModelProviderFactory(homeRepository))
            .get(HomeViewModel::class.java)
        setHasOptionsMenu(true)
        observeNews()


    }

    private fun observeNews() {

        homeViewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d(TAG, "onViewCreated: Success - $response. Results - ${response.data}")
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    response.data?.response?.results?.let {
                        homeAdapter.submitList(it)
                        Log.d(TAG, "onViewCreated: array size - ${it.size}")
                    }
                }
                is Resource.Error -> {
                    Log.d(TAG, "onViewCreated: Error - ${response.message}")
                    home_progress.visibility = View.GONE
                    home_srl.isRefreshing = false
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    Log.d(TAG, "onViewCreated: Loading...")
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
            homeViewModel.searchNews()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.home_menu, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setQuery(homeViewModel.query, false)
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
                        homeViewModel.query = it // TODO Hardcode
                    }
                    homeViewModel.searchNews()
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
                            homeViewModel.filter
                        ) { dialog, which ->
                            if (homeViewModel.filter != which) {
                                homeViewModel.filter = which
                                homeViewModel.searchNews()
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


}