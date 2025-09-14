package com.example.horizon.ui.fragment.peerconnect

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horizon.R
import com.google.android.material.card.MaterialCardView
import kotlin.math.absoluteValue

class UserAdapter(
    private val users: MutableList<String>,
    private val onUserClick: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var selectedPosition = -1

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userStatus: ImageView = itemView.findViewById(R.id.userStatus)
        private val userCard: MaterialCardView = itemView.findViewById(R.id.userCard)

        fun bind(user: String, position: Int) {
            userName.text = user

            // Set selection state
            userCard.isSelected = position == selectedPosition
            userCard.strokeColor = if (position == selectedPosition) {
                ContextCompat.getColor(itemView.context, R.color.primary_green)
            } else {
                ContextCompat.getColor(itemView.context, R.color.stroke_gray)
            }

            // Set user avatar (you can customize this based on user data)
            val avatarDrawable = generateAvatarDrawable(user)
            userAvatar.setImageDrawable(avatarDrawable)

            // Online status indicator
            userStatus.setImageResource(R.drawable.ic_online_status)

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onUserClick(user)
            }
        }

        private fun generateAvatarDrawable(username: String): Drawable {
            val colors = arrayOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8")
            val color = Color.parseColor(colors[username.hashCode().absoluteValue % colors.size])

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(color)

            return drawable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<String>) {
        users.clear()
        users.addAll(newUsers)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedUser(): String? {
        return if (selectedPosition >= 0 && selectedPosition < users.size) {
            users[selectedPosition]
        } else null
    }
}