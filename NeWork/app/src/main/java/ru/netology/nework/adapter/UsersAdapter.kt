package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.netology.nework.databinding.ItemUserBinding
import ru.netology.nework.dto.UserPreview

class UsersAdapter(private val onUserClick: (UserPreview) -> Unit) :
    ListAdapter<UserPreview, UsersAdapterViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapterViewHolder {
        val binding =
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersAdapterViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UsersAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UsersAdapterViewHolder(private val binding: ItemUserBinding, private val onUserClick: (UserPreview) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserPreview) {
        binding.avatar.load(user.avatar)
        binding.name.text = user.name
        binding.root.setOnClickListener { onUserClick(user) }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserPreview>() {
    override fun areItemsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserPreview, newItem: UserPreview): Boolean {
        return oldItem == newItem
    }
}
