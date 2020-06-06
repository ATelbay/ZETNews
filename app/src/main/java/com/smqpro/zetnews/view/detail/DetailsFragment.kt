package com.smqpro.zetnews.view.detail

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.smqpro.zetnews.R
import com.smqpro.zetnews.util.htmlParse
import com.smqpro.zetnews.util.load
import com.smqpro.zetnews.util.prettyTime
import com.smqpro.zetnews.util.sendShareIntent
import com.viven.imagezoom.ImageZoomHelper
import kotlinx.android.synthetic.main.details_fragment.*
import kotlinx.android.synthetic.main.news_item.*

class DetailsFragment : Fragment(R.layout.details_fragment) {
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
//            ImageZoomHelper.setViewZoomable(description_image)
        }

        setNews()

    }


    private fun setNews() =
        args.result.apply {
            description_image.load(fields.thumbnail, false) {
                startPostponedEnterTransition()
            }
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