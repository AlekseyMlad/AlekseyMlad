package ru.netology.nework.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.LikerAvatarsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostDetailsBinding
import ru.netology.nework.viewmodel.PostDetailsViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostDetailsViewModel by viewModels()
    private val postViewModel: PostViewModel by activityViewModels()

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)

        val likersAvatarsAdapter = LikerAvatarsAdapter(
            onAvatarClick = { user ->
                findNavController().navigate(
                    R.id.action_global_userDetailsFragment,
                    bundleOf("userId" to user.id)
                )
            },
            onPlusClick = { 
                viewModel.post.value?.let { post ->
                    findNavController().navigate(
                        R.id.action_postDetailsFragment_to_likersListFragment,
                        bundleOf("likerIds" to post.likeOwnerIds.toLongArray())
                    )
                }
            }
        )
        binding.likersAvatars.adapter = likersAvatarsAdapter

        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post == null) {
                findNavController().navigateUp()
                return@observe
            }
            post.let {
                binding.author.text = it.author
                binding.published.text = it.published
                binding.content.text = it.content
                binding.avatar.load(it.authorAvatar)
                binding.attachment.load(it.attachment?.url)
                binding.attachment.isVisible = it.attachment != null
                binding.authorJob.text = it.authorJob ?: "В поиске работы"

                binding.like.isChecked = it.likedByMe
                binding.likeCount.text = it.likeOwnerIds.size.toString()

                val likers = it.likeOwnerIds.mapNotNull { id -> it.users[id] }
                likersAvatarsAdapter.submitList(likers)

                binding.menu.isVisible = it.authorId == appAuth.authState.value.id

                it.coords?.let {
                    binding.map.isVisible = true
                    val map = binding.map.mapWindow.map
                    map.move(
                        CameraPosition(Point(it.lat, it.long), 15.0f, 0.0f, 0.0f)
                    )
                                    map.mapObjects.addPlacemark().apply {
                        geometry = Point(it.lat, it.long)
                        setIcon(com.yandex.runtime.image.ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on))
                    }
                } ?: run {
                    binding.map.isVisible = false
                }
            }
        }

        binding.like.setOnClickListener {
            viewModel.post.value?.let { post ->
                if (post.likedByMe) {
                    postViewModel.unlikeById(post.id)
                } else {
                    postViewModel.likeById(post.id)
                }
            }
        }

        binding.menu.setOnClickListener { view ->
            viewModel.post.value?.let { post ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.post_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                findNavController().navigate(
                                    R.id.action_global_editPostFragment, // Using global action
                                    bundleOf("postId" to post.id)
                                )
                                true
                            }
                            R.id.action_remove -> {
                                postViewModel.removeById(post.id)
                                findNavController().navigateUp()
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.share_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share -> {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, viewModel.post.value?.content)
                            type = "text/plain"
                        }
                        val shareIntent =
                            Intent.createChooser(intent, getString(R.string.share))
                        startActivity(shareIntent)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.map.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}