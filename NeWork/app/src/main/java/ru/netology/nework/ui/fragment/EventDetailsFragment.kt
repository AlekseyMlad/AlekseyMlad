package ru.netology.nework.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
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
import ru.netology.nework.databinding.FragmentEventDetailsBinding
import ru.netology.nework.viewmodel.EventDetailsViewModel
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailsFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventDetailsViewModel by viewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.share_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share -> {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, viewModel.event.value?.content)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(intent, getString(R.string.share))
                        startActivity(shareIntent)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        val likersAvatarsAdapter = LikerAvatarsAdapter(
            onAvatarClick = { user ->
                findNavController().navigate(
                    R.id.action_global_userDetailsFragment,
                    bundleOf("userId" to user.id)
                )
            },
            onPlusClick = { 
                viewModel.event.value?.let { event ->
                    findNavController().navigate(
                        R.id.action_eventDetailsFragment_to_likersListFragment,
                        bundleOf("likerIds" to event.likeOwnerIds.toLongArray())
                    )
                }
            }
        )
        binding.likersAvatars.adapter = likersAvatarsAdapter

        val speakerAvatarsAdapter = LikerAvatarsAdapter(
            onAvatarClick = { user ->
                findNavController().navigate(
                    R.id.action_global_userDetailsFragment,
                    bundleOf("userId" to user.id)
                )
            },
            onPlusClick = { 
                viewModel.event.value?.let { event ->
                    findNavController().navigate(
                        R.id.action_eventDetailsFragment_to_speakersListFragment,
                        bundleOf("speakerIds" to event.speakerIds.toLongArray())
                    )
                }
            }
        )
        binding.speakersAvatars.adapter = speakerAvatarsAdapter

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event?.let {
                binding.author.text = it.author
                binding.published.text = it.published
                binding.content.text = it.content
                binding.avatar.load(it.authorAvatar)
                binding.attachment.load(it.attachment?.url)
                binding.attachment.isVisible = it.attachment != null
                binding.authorJob.text = it.authorJob ?: "В поиске работы"
                binding.datetime.text = it.datetime
                binding.type.text = it.type?.toString() ?: ""

                binding.like.isChecked = it.likedByMe
                binding.likeCount.text = it.likeOwnerIds.size.toString()
                binding.participate.isChecked = it.participatedByMe
                binding.participantsCount.text = it.participantsIds.size.toString()

                binding.menu.isVisible = it.authorId == appAuth.authState.value.id

                val likers = it.likeOwnerIds.mapNotNull { id -> it.users[id] }
                likersAvatarsAdapter.submitList(likers)

                val speakers = it.speakerIds.mapNotNull { id -> it.users[id] }
                speakerAvatarsAdapter.submitList(speakers)

                it.coords?.let { coords ->
                    binding.map.isVisible = true
                    val map = binding.map.mapWindow.map
                    map.move(
                        CameraPosition(Point(coords.lat, coords.long), 15.0f, 0.0f, 0.0f)
                    )
                    map.mapObjects.addPlacemark().apply {
                        geometry = Point(coords.lat, coords.long)
                        setIcon(com.yandex.runtime.image.ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on))
                    }
                } ?: run {
                    binding.map.isVisible = false
                }
            }
        }

        binding.like.setOnClickListener {
            viewModel.event.value?.let { event ->
                if (event.likedByMe) {
                    eventViewModel.unlikeById(event.id)
                } else {
                    eventViewModel.likeById(event.id)
                }
                viewModel.loadEvent()
            }
        }

        binding.participate.setOnClickListener {
             viewModel.event.value?.let { event ->
                if (event.participatedByMe) {
                    eventViewModel.unparticipate(event.id)
                } else {
                    eventViewModel.participate(event.id)
                }
                viewModel.loadEvent()
            }
        }

        binding.menu.setOnClickListener { view ->
            viewModel.event.value?.let { event ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.event_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                findNavController().navigate(
                                    R.id.action_global_editEventFragment,
                                    bundleOf("eventId" to event.id)
                                )
                                true
                            }
                            R.id.action_remove -> {
                                eventViewModel.removeById(event.id)
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