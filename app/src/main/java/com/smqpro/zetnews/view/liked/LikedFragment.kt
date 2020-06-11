package com.smqpro.zetnews.view.liked

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.TAG
import com.smqpro.zetnews.util.sendShareIntent
import com.smqpro.zetnews.view.MainActivity
import com.smqpro.zetnews.view.home.HomeFragmentDirections
import com.smqpro.zetnews.view.home.HomeListAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_liked.*
import kotlinx.android.synthetic.main.news_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LikedFragment : Fragment(R.layout.fragment_liked), HomeListAdapter.Interaction {
    private lateinit var homeAdapter: HomeListAdapter
    private lateinit var viewModel: LikedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initRecycler()
        observeLikedNews()
    }

    private fun initViewModel() {
        val repository = LikedRepository((activity as MainActivity).db)
        viewModel =
            ViewModelProvider((activity as MainActivity), LikedViewModelProviderFactory(repository))
                .get(LikedViewModel::class.java)
    }

    private fun initRecycler() {
        homeAdapter = HomeListAdapter(this)
        liked_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    private fun observeLikedNews() {
        viewModel.getLikedNews().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeLikedNews: result list - ${it.size}")
            if (it.isNullOrEmpty()) {
                no_liked_news_text.visibility = View.VISIBLE
                liked_recycler.visibility = View.INVISIBLE
            } else {
                no_liked_news_text.visibility = View.INVISIBLE
                liked_recycler.visibility = View.VISIBLE
                homeAdapter.submitList(it)
            }
        })
    }

    override fun onItemSelected(position: Int, item: Result, itemView: View) {
        Log.d(TAG, "onItemSelected: clicked")
        CoroutineScope(Dispatchers.Default).launch {
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
                val destination = LikedFragmentDirections.toDetailsFragment(item)
                Log.d("TAG", "${item.fields.thumbnail} ${item.webTitle}")
                findNavController().navigate(destination, extras)
            }
        }
    }

    override fun onShareSelected(url: String) {
        Log.d(TAG, "onShareSelected: clicked")
        context?.sendShareIntent(url)
    }

    override fun onLongItemSelected(item: Result) {
        Log.d(TAG, "onLongItemSelected: clicked")
        val destination = LikedFragmentDirections.toWebFragment(item)
        findNavController().navigate(destination)
    }

}