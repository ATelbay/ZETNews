package com.smqpro.zetnews.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smqpro.zetnews.R
import com.smqpro.zetnews.model.response.Result
import com.smqpro.zetnews.util.htmlParse
import com.smqpro.zetnews.util.prettyTime
import kotlinx.android.synthetic.main.news_item.view.*


class HomeListAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Result>() {
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return HomeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.news_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HomeViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Result>) {
        differ.submitList(list)
    }

    class HomeViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Result) = with(itemView) {
            val author = if (item.tags.isNotEmpty()) item.tags[0].webTitle else ""

            item.apply {
                news_title.text = webTitle
                news_category.text = sectionName
                news_description.text = htmlParse(fields.trailText)
                Glide.with(context)
                    .load(item.fields.thumbnail)
                    .into(news_image)
                news_author.text = author
                news_time.text = prettyTime(webPublicationDate)

                ViewCompat.setTransitionName(news_title, webTitle)
                ViewCompat.setTransitionName(news_description, fields.trailText)
                ViewCompat.setTransitionName(news_image, fields.thumbnail)
                ViewCompat.setTransitionName(news_author, author)
                ViewCompat.setTransitionName(news_time, webPublicationDate)
                ViewCompat.setTransitionName(news_category, sectionName)
            }



            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item, this)
            }

            itemView.setOnLongClickListener {
                interaction?.onLongItemSelected(item)
                return@setOnLongClickListener true
            }

            news_share.setOnClickListener {
                interaction?.onShareSelected(item.webUrl)
            }

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Result, itemView: View)
        fun onShareSelected(url: String)
        fun onLongItemSelected(item: Result)
    }
}

