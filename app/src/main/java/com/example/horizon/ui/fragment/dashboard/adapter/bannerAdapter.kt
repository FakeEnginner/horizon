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

class bannerAdapter : ListAdapter<bannerModel, bannerAdapter.BannerViewHolder>(bannerDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textView3)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(item: bannerModel) {
            titleTextView.text = item.title
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .error(R.drawable.pic_banner)
                .into(imageView)
        }
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class bannerDiffCallback : DiffUtil.ItemCallback<bannerModel>() {
        override fun areItemsTheSame(oldItem: bannerModel, newItem: bannerModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: bannerModel, newItem: bannerModel): Boolean {
            return oldItem == newItem
        }
    }
}