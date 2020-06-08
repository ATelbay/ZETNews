package com.smqpro.zetnews.view.detail

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.*
import com.viven.imagezoom.ImageZoomHelper
import kotlinx.android.synthetic.main.fragment_details.*

class DetailsFragment : Fragment(R.layout.fragment_details) {
    private val args: DetailsFragmentArgs by navArgs()

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

        setNews()

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
            description_description.text = htmlParse(fields.trailText)
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