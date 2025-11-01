package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.ItemEventBinding
import ru.netology.nework.databinding.ItemPostBinding
import ru.netology.nework.databinding.ItemUserBinding
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.JobItem
import ru.netology.nework.dto.PostItem
import ru.netology.nework.dto.UserItem
import ru.netology.nework.dto.UserResponse
import coil.load
import androidx.core.view.isVisible
import ru.netology.nework.databinding.ItemJobBinding
import ru.netology.nework.util.formatDate

interface OnInteractionListener {
    fun onEdit(item: DisplayableItem) {}
    fun onRemove(item: DisplayableItem) {}
    fun onItemClick(item: DisplayableItem) {}
    fun onLike(item: DisplayableItem) {}
    fun onParticipate(item: DisplayableItem) {}
}

interface SelectionInteractionListener : OnInteractionListener {
    fun isUserSelected(userId: Long): Boolean
}

class FeedAdapter(private val onInteractionListener: OnInteractionListener, private val isProfileOwner: Boolean = false) :
    ListAdapter<DisplayableItem, RecyclerView.ViewHolder>(FeedDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostItem -> VIEW_TYPE_POST
            is UserItem -> VIEW_TYPE_USER
            is EventItem -> VIEW_TYPE_EVENT
            is JobItem -> VIEW_TYPE_JOB
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_POST -> {
                val binding = ItemPostBinding.inflate(inflater, parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            VIEW_TYPE_USER -> {
                val binding = ItemUserBinding.inflate(inflater, parent, false)
                FeedUserViewHolder(binding, onInteractionListener)
            }
            VIEW_TYPE_EVENT -> {
                val binding = ItemEventBinding.inflate(inflater, parent, false)
                EventViewHolder(binding, onInteractionListener)
            }
            VIEW_TYPE_JOB -> {
                val binding = ItemJobBinding.inflate(inflater, parent, false)
                JobViewHolder(binding, onInteractionListener)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PostItem -> (holder as PostViewHolder).bind(item)
            is UserItem -> {
                val isSelected = (onInteractionListener as? SelectionInteractionListener)?.isUserSelected(item.user.id) ?: false
                (holder as FeedUserViewHolder).bind(item.user, isSelected)
            }
            is EventItem -> (holder as EventViewHolder).bind(item)
            is JobItem -> (holder as JobViewHolder).bind(item, isProfileOwner)
        }
    }

    companion object {
        private const val VIEW_TYPE_POST = 1
        private const val VIEW_TYPE_USER = 2
        private const val VIEW_TYPE_EVENT = 3
        private const val VIEW_TYPE_JOB = 4
    }
}

class PostViewHolder(
    private val binding: ItemPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PostItem) {
        val post = item.post
        binding.author.text = post.author
        binding.published.text = formatDate(post.published)
        binding.content.text = post.content
        binding.like.isChecked = post.likedByMe
        binding.like.text = post.likeOwnerIds.size.toString()
        binding.avatar.load(post.authorAvatar)
        binding.attachment.load(post.attachment?.url)
        binding.attachment.isVisible = post.attachment != null

        post.link?.let {
            binding.link.text = it
            binding.link.isVisible = true
        } ?: run {
            binding.link.isVisible = false
        }

        binding.like.setOnClickListener {
            onInteractionListener.onLike(item)
        }

        binding.root.setOnClickListener {
            onInteractionListener.onItemClick(item)
        }

        binding.menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onInteractionListener.onEdit(item)
                            true
                        }
                        R.id.action_remove -> {
                            onInteractionListener.onRemove(item)
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
    }
}

class FeedUserViewHolder(
    private val binding: ItemUserBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserResponse, isSelected: Boolean) {
        binding.login.text = user.login
        binding.name.text = user.name
        binding.root.setBackgroundColor(
            if (isSelected) itemView.context.getColor(R.color.selected_user_background) else android.graphics.Color.TRANSPARENT
        )

        binding.root.setOnClickListener {
            onInteractionListener.onItemClick(UserItem(user))
        }
    }
}

class EventViewHolder(
    private val binding: ItemEventBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: EventItem) {
        val event = item.event
        binding.author.text = event.author
        binding.published.text = formatDate(event.published)
        binding.content.text = event.content
        binding.datetime.text = formatDate(event.datetime)
        binding.type.text = event.type.toString()
        binding.like.isChecked = event.likedByMe
        binding.like.text = event.likeOwnerIds.size.toString()
        binding.participate.isChecked = event.participatedByMe
        binding.participate.text = event.participantsIds.size.toString()
        binding.avatar.load(event.authorAvatar)

        event.attachment?.let {
            binding.attachment.load(it.url)
            binding.attachment.isVisible = true
        } ?: run {
            binding.attachment.isVisible = false
        }

        event.link?.let {
            binding.link.text = it
            binding.link.isVisible = true
        } ?: run {
            binding.link.isVisible = false
        }
        binding.like.setOnClickListener {
            onInteractionListener.onLike(item)
        }

        binding.participate.setOnClickListener {
            onInteractionListener.onParticipate(item)
        }

        binding.root.setOnClickListener {
            onInteractionListener.onItemClick(item)
        }

        binding.menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.event_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onInteractionListener.onEdit(item)
                            true
                        }
                        R.id.action_remove -> {
                            onInteractionListener.onRemove(item)
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
    }
}

class JobViewHolder(
    private val binding: ItemJobBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: JobItem, isOwner: Boolean) {
        val job = item.job
        binding.company.text = job.name
        binding.position.text = job.position
        val formattedStart = formatDate(job.start)
        val formattedFinish = job.finish?.let { formatDate(it) } ?: "..."
        binding.experience.text = "$formattedStart - $formattedFinish"

        binding.menu.isVisible = isOwner
        binding.menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.job_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onInteractionListener.onEdit(item)
                            true
                        }
                        R.id.action_remove -> {
                            onInteractionListener.onRemove(item)
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
    }
}

class FeedDiffCallback : DiffUtil.ItemCallback<DisplayableItem>() {
    override fun areItemsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
        return oldItem == newItem
    }
}