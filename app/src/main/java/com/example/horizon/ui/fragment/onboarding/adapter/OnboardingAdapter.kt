package com.example.horizon.ui.fragment.onboarding.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.horizon.R
import com.example.horizon.ui.fragment.onboarding.models.OnboardingPage

class OnboardingAdapter(private val onboardingPages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val page = onboardingPages[position]
        holder.bind(page)
    }

    override fun getItemCount(): Int {
        return onboardingPages.size
    }

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(onboardingPage: OnboardingPage) {
            Glide.with(itemView.context)
                .load(onboardingPage.imageResId)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.yoga)
                .into(imageView)
            descriptionTextView.text = onboardingPage.description
        }
    }
}
