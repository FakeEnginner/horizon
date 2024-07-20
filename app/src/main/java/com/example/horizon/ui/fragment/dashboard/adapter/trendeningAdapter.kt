package com.example.horizon.ui.fragment.dashboard.adapter

import android.annotation.SuppressLint
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
import com.example.horizon.model.trendeningModel

class trendeningAdapter : ListAdapter<trendeningModel,trendeningAdapter.trendeningViewHolder>(trendeningDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): trendeningViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trending, parent, false)
        return trendeningViewHolder(view)
    }

    class trendeningViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = itemView.findViewById(R.id.blog_txt)
        private val imageView: ImageView = itemView.findViewById(R.id.banner_img)

        fun bind(item: trendeningModel) {
            titleTextView.text = item.title
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .error(R.drawable.pic_banner)
                .into(imageView)
        }
    }

    override fun onBindViewHolder(holder: trendeningViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class trendeningDiffCallback : DiffUtil.ItemCallback<trendeningModel>() {
        override fun areItemsTheSame(oldItem: trendeningModel, newItem: trendeningModel): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: trendeningModel,
            newItem: trendeningModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}