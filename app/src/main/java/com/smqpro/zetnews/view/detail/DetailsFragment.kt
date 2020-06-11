package com.smqpro.zetnews.view.detail

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.*
import com.smqpro.zetnews.view.MainActivity
import com.viven.imagezoom.ImageZoomHelper
import kotlinx.android.synthetic.main.fragment_details.*

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var viewModel: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)

        postponeEnterTransition()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initViewModel()
        setTransitionNames()
        setNews()
        initFab()
    }

    private fun initViewModel() {
        val repository = DetailsRepository((activity as MainActivity).db)
        viewModel =
            ViewModelProvider(
                (activity as MainActivity),
                DetailsViewModelProviderFactory(repository)
            )
                .get(DetailsViewModel::class.java)
    }

    private fun setTransitionNames() {
        args.result.apply {
            val author = if (tags.isNotEmpty()) tags[0].webTitle else ""
            ViewCompat.setTransitionName(description_title, webTitle)
            ViewCompat.setTransitionName(description_description, fields.trailText)
            ViewCompat.setTransitionName(description_image, fields.thumbnail)
            ViewCompat.setTransitionName(description_author, author)
            ViewCompat.setTransitionName(description_time, webPublicationDate)
            ViewCompat.setTransitionName(description_category, sectionName)
            initButton()
            ImageZoomHelper.setViewZoomable(description_image)
        }
    }

    private fun setNews() =
        args.result.apply {
            description_image.load(fields.thumbnail, false) {
                startPostponedEnterTransition()
                context?.apply {
                    val px: Int = dpToPixels(12F).toInt() + it
                    description_fl.minimumHeight = px
                }
            }
            description_fl.minimumHeight = description_image.height
            Log.d(
                TAG, "setNews: fl's min height - ${description_fl.minimumHeight},\n" +
                        "image's height - ${description_image.height}"
            )
            description_title.text = webTitle
            description_description.text = htmlParse(fields.trailText + getString(R.string.text_sample))
            Html.fromHtml(fields.trailText).toString()

            description_author.text = if (tags.isNotEmpty()) tags[0].webTitle else ""
            description_time.text = prettyTime(webPublicationDate)
            description_category.text = sectionName

        }

    private fun initButton() {
        details_website_button.setOnClickListener {
            val destination = DetailsFragmentDirections.toWebFragment(args.result)
            findNavController().navigate(destination)
        }
    }

    private fun initFab() {
        val anim = loadAnimation(context, R.anim.rotate)
        val animDislike = ObjectAnimator.ofFloat(details_fab, "rotation", 0F, 225F)
        val animLike = ObjectAnimator.ofFloat(details_fab, "rotation", 225F, 0F)
        anim.fillAfter = true
        if (args.result.liked) {
            animDislike.start()
        }
        details_fab.setOnClickListener {
            if (args.result.liked) {
                args.result.liked = false
                viewModel.upsertNews(args.result)
                animLike.start()
            } else {
                args.result.liked = true
                viewModel.upsertNews(args.result)
                animDislike.start()
            }
            Log.d(TAG, "initFab: news liked - ${args.result.liked}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> context?.sendShareIntent(args.result.webUrl)
        }
        return super.onOptionsItemSelected(item)
    }


}