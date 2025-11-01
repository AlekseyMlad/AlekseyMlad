package ru.netology.nework.ui.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.FabAction
import ru.netology.nework.dto.PostItem
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.SharedViewModel

@AndroidEntryPoint
class PostsFragment : FeedFragment(), OnInteractionListener {
    override val viewModel: PostViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    override val adapter by lazy { FeedAdapter(this) }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setFabAction(FabAction.NEW_POST)
    }

    override fun onPause() {
        super.onPause()
        sharedViewModel.setFabAction(FabAction.NONE)
    }

    override fun onEdit(item: DisplayableItem) {
        if (item !is PostItem) return
        findNavController().navigate(
            R.id.action_postsFragment_to_newPostFragment,
            bundleOf("postId" to item.post.id)
        )
    }

    override fun onRemove(item: DisplayableItem) {
        if (item !is PostItem) return
        viewModel.removeById(item.post.id)
    }

    override fun onItemClick(item: DisplayableItem) {
        if (item !is PostItem) return
        findNavController().navigate(
            R.id.action_postsFragment_to_postDetailsFragment,
            bundleOf("postId" to item.post.id)
        )
    }

    override fun onLike(item: DisplayableItem) {
        if (item !is PostItem) return
        if (item.post.likedByMe) {
            viewModel.unlikeById(item.post.id)
        } else {
            viewModel.likeById(item.post.id)
        }
    }
}
