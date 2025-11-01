package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.google.android.material.snackbar.Snackbar
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.util.toFile
import ru.netology.nework.viewmodel.NewPostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private val viewModel: NewPostViewModel by viewModels()

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        val postId = arguments?.getLong("postId")
        if (postId != null && postId != 0L) {
            viewModel.loadPost(postId)
        }

        viewModel.edited.observe(viewLifecycleOwner) {
            binding.editText.setText(it.content)
            binding.authorJob.setText(it.authorJob)
        }

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.changeContent(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.authorJob.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.changeAuthorJob(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val attachmentLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    val stream = requireContext().contentResolver.openInputStream(it)
                    val type = when (requireContext().contentResolver.getType(it)) {
                        in listOf("image/jpeg", "image/png") -> AttachmentType.IMAGE
                        in listOf("video/mp4") -> AttachmentType.VIDEO
                        in listOf("audio/mpeg") -> AttachmentType.AUDIO
                        else -> {
                            Snackbar.make(binding.root, R.string.unsupported_media_type, Snackbar.LENGTH_LONG).show()
                            null
                        }
                    }
                    type?.let {
                        viewModel.changeMedia(uri, uri.toFile(requireContext()), type)
                    }
                }
            }

        binding.clearButton.setOnClickListener {
            viewModel.clearMedia()
        }

        viewModel.media.observe(viewLifecycleOwner) { media ->
            binding.previewContainer.isVisible = media.uri != null
            binding.preview.isVisible = media.type == AttachmentType.IMAGE || media.type == AttachmentType.VIDEO
            if (binding.preview.isVisible) {
                binding.preview.setImageURI(media.uri)
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            binding.map.isVisible = post.coords != null
            if (post.coords != null) {
                binding.map.mapWindow.map.mapObjects.clear()
                binding.map.mapWindow.map.move(
                    CameraPosition(
                        Point(post.coords.lat, post.coords.long),
                        15.0f,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 5f),
                    null
                )
                binding.map.mapWindow.map.mapObjects.addPlacemark(
                    Point(post.coords.lat, post.coords.long),
                    ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on)
                )
            }
        }

        binding.clearLocationButton.setOnClickListener {
            viewModel.clearCoordinates()
            binding.map.visibility = View.GONE
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_item_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.save -> {
                    viewModel.save()
                    true
                }

                else -> false
            }
        }, viewLifecycleOwner)

        binding.newItemBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_photo -> {
                    attachmentLauncher.launch("image/*")
                    true
                }
                R.id.action_attach -> {
                    attachmentLauncher.launch("*/*")
                    true
                }
                R.id.action_people -> {
                    val bundle = Bundle().apply { putBoolean("isSelectionMode", true) }
                    findNavController().navigate(R.id.usersFragment, bundle)
                    true
                }
                R.id.action_location -> {
                    findNavController().navigate(R.id.action_newPostFragment_to_mapPickerFragment)
                    true
                }
                else -> false
            }
        }

        setFragmentResultListener("map_picker_result") { _, bundle ->
            val lat = bundle.getDouble("lat")
            val long = bundle.getDouble("long")
            viewModel.setCoordinates(lat, long)
        }

        viewModel.mediaError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMediaError()
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.postCreationFailed.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG).show()
        }

        setFragmentResultListener("users_picker_result") { _, bundle ->

            val userIds = bundle.getLongArray("userIds")

            viewModel.setMentioned(userIds?.toList())

        }
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        binding.map.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
