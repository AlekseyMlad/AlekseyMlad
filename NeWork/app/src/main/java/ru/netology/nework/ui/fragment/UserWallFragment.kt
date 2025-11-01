package ru.netology.nework.ui.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.PostItem
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserWallViewModel

@AndroidEntryPoint
class UserWallFragment : FeedFragment(), OnInteractionListener {


    override val viewModel: UserWallViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()

    override val adapter by lazy { FeedAdapter(this) }


    override fun onEdit(item: DisplayableItem) {
        if (item !is PostItem) return
        findNavController().navigate(
            R.id.action_global_editPostFragment,
            bundleOf("postId" to item.post.id)
        )
    }

    override fun onRemove(item: DisplayableItem) {
        if (item !is PostItem) return
        postViewModel.removeById(item.post.id)
        viewModel.load() // Refresh wall
    }

    override fun onItemClick(item: DisplayableItem) {
        if (item !is PostItem) return
    }

    override fun onLike(item: DisplayableItem) {
        if (item !is PostItem) return
        if (item.post.likedByMe) {
            postViewModel.unlikeById(item.post.id)
        } else {
            postViewModel.likeById(item.post.id)
        }
        viewModel.load()
    }
}
