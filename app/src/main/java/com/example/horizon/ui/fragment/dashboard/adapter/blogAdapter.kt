package com.example.horizon.ui.fragment.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.horizon.R
import com.example.horizon.model.bannerModel
import com.example.horizon.model.blogModel

class blogAdapter : ListAdapter<blogModel, blogAdapter.blogViewHolder>(blogDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): blogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blogs, parent, false)
        return blogViewHolder(view)
    }

    class blogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = itemView.findViewById(R.id.blog_txt)
        private val imageView: ImageView = itemView.findViewById(R.id.banner_img)

        fun bind(item: blogModel) {
            titleTextView.text = item.title
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .error(R.drawable.pic_banner)
                .into(imageView)
        }

    }
   override fun onBindViewHolder(holder: blogViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class blogDiffCallback : DiffUtil.ItemCallback<blogModel>() {
        override fun areItemsTheSame(oldItem: blogModel, newItem: blogModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: blogModel, newItem: blogModel): Boolean {
            return oldItem == newItem
        }
    }
}


