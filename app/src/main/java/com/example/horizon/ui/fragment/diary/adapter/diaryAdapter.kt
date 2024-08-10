package com.example.horizon.ui.fragment.diary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.horizon.R
import com.example.horizon.model.entities.diary

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
        private val titleTextView: TextView = itemView.findViewById(R.id.textTitle)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.textSubtitle)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.textDateTime)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(diaryList[position])
                }
            }
        }

        fun bind(diary: diary) {
            titleTextView.text = diary.title
            subtitleTextView.text = diary.subtitle
            dateTimeTextView.text = diary.dateTime
        }
    }
    interface OnItemClickListener {
        fun onItemClick(diary: diary)
    }
}
