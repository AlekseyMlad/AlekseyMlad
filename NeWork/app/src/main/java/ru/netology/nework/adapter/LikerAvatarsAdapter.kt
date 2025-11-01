package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.netology.nework.R
import ru.netology.nework.databinding.ItemAvatarBinding
import ru.netology.nework.dto.UserPreview

class LikerAvatarsAdapter(
    private val onAvatarClick: (UserPreview) -> Unit,
    private val onPlusClick: () -> Unit
) : ListAdapter<UserPreview, RecyclerView.ViewHolder>(DiffCallback) {

    private val maxAvatars = 5

    override fun getItemViewType(position: Int): Int {
        return if (itemCount > maxAvatars && position == maxAvatars) {
            PLUS_VIEW_TYPE
        } else {
            AVATAR_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            AVATAR_VIEW_TYPE -> {
                val binding = ItemAvatarBinding.inflate(inflater, parent, false)
                AvatarViewHolder(binding, onAvatarClick)
            }
            PLUS_VIEW_TYPE -> {
                val binding = ItemAvatarBinding.inflate(inflater, parent, false)
                PlusViewHolder(binding, onPlusClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AvatarViewHolder -> holder.bind(getItem(position))
            is PlusViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return if (super.getItemCount() > maxAvatars) maxAvatars + 1 else super.getItemCount()
    }

    class AvatarViewHolder(
        private val binding: ItemAvatarBinding,
        private val onAvatarClick: (UserPreview) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserPreview) {
            binding.root.load(user.avatar)
            binding.root.setOnClickListener {
                onAvatarClick(user)
            }
        }
    }

    class PlusViewHolder(
        private val binding: ItemAvatarBinding,
        private val onPlusClick: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.root.setImageResource(R.drawable.ic_add)
            binding.root.setOnClickListener {
                onPlusClick()
            }
        }
    }

    companion object {
        private const val AVATAR_VIEW_TYPE = 0
        private const val PLUS_VIEW_TYPE = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<UserPreview>() {
            override fun areItemsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
                return oldItem == newItem
            }
        }
    }
}
