package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.netology.nework.databinding.ItemUserBinding
import ru.netology.nework.dto.UserPreview

class MentionedUsersAdapter(private val onUserClick: (UserPreview) -> Unit) :
    ListAdapter<UserPreview, MentionedUserViewHolder>(MentionedUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionedUserViewHolder {
        val binding =
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentionedUserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: MentionedUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MentionedUserViewHolder(private val binding: ItemUserBinding, private val onUserClick: (UserPreview) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserPreview) {
        binding.root.setOnClickListener { onUserClick(user) }
        binding.avatar.load(user.avatar)
        binding.name.text = user.name
        binding.login.isVisible = false
    }
}

class MentionedUserDiffCallback : DiffUtil.ItemCallback<UserPreview>() {
    override fun areItemsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem == newItem
    }
}
