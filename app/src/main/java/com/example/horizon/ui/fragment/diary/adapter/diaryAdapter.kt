package com.example.horizon.ui.fragment.diary.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.horizon.R
import com.example.horizon.model.entities.diary
import java.io.File

class diaryAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<diaryAdapter.DiaryViewHolder>() {

    private var diaryList = emptyList<diary>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_container_diary, parent, false)
        return DiaryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val currentDiary = diaryList[position]
        holder.bind(currentDiary)
    }

    override fun getItemCount() = diaryList.size

    fun setDiaries(diaries: List<diary>) {
        diaryList = diaries
        notifyDataSetChanged()
    }

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardDiary)
        private val colorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        private val titleTextView: TextView = itemView.findViewById(R.id.textTitle)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.textSubtitle)
        private val notePreviewTextView: TextView = itemView.findViewById(R.id.textNotePreview)
        private val webLinkIndicator: ImageView = itemView.findViewById(R.id.imageWebLink)
        private val imageAttachmentIndicator: ImageView = itemView.findViewById(R.id.imageAttachment)
        private val imageContainer: FrameLayout = itemView.findViewById(R.id.imageContainer)
        private val diaryImage: ImageView = itemView.findViewById(R.id.imageDiary)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(diaryList[position])
                }
            }
        }

        fun bind(diary: diary) {
            // Set main content
            titleTextView.text = diary.title
            subtitleTextView.text = diary.subtitle

            // Set note preview
            notePreviewTextView.text = diary.noteText
            notePreviewTextView.visibility = if (TextUtils.isEmpty(diary.noteText)) View.GONE else View.VISIBLE

            // Set color indicator
            if (!diary.color.isNullOrEmpty()) {
                try {
                    colorIndicator.setBackgroundColor(Color.parseColor(diary.color))
                } catch (e: IllegalArgumentException) {
                    colorIndicator.setBackgroundColor(Color.BLUE)
                }
            } else {
                colorIndicator.setBackgroundColor(Color.BLUE)
            }

            // Handle image
            if (!diary.imagePath.isNullOrEmpty()) {
                try {
                    val imageFile = File(diary.imagePath)
                    if (imageFile.exists()) {
                        // Load and display the image
                        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                        diaryImage.setImageBitmap(bitmap)
                        imageContainer.visibility = View.VISIBLE

                        // Hide the image indicator as we're showing the actual image
                        imageAttachmentIndicator.visibility = View.GONE
                    } else {
                        // Image file doesn't exist, show indicator instead
                        imageContainer.visibility = View.GONE
                        imageAttachmentIndicator.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    // Error loading image, show indicator
                    imageContainer.visibility = View.GONE
                    imageAttachmentIndicator.visibility = View.VISIBLE
                }
            } else {
                // No image path, hide both
                imageContainer.visibility = View.GONE
                imageAttachmentIndicator.visibility = View.GONE
            }

            // Show web link indicator if present
            webLinkIndicator.visibility = if (diary.webLink.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    interface OnItemClickListener {
        fun onItemClick(diary: diary)
    }
}