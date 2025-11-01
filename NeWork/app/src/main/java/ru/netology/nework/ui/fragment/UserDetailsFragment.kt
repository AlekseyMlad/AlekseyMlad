package ru.netology.nework.ui.fragment

import ru.netology.nework.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUserDetailsBinding
import ru.netology.nework.viewmodel.UserDetailsViewModel
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsFragment : Fragment() {

    private val titles = arrayOf("Wall", "Jobs")
    private val viewModel: UserDetailsViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    private var _binding: FragmentUserDetailsBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<String>

    private fun createTempFile(): Uri {
        val file = File(requireContext().filesDir, "avatar.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.share_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share -> {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, viewModel.user.value?.name)
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

        val userId = arguments?.getLong("userId") ?: 0L

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = if (appAuth.authState.value.id == userId) View.VISIBLE else View.GONE

        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uri -> viewModel.changeAvatar(uri) }
            }
        }

        pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.changeAvatar(it) }
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    photoUri = createTempFile()
                    takePhotoLauncher.launch(photoUri ?: Uri.EMPTY)
                }
            }

        viewModel.user.observe(viewLifecycleOwner) {
            binding.toolbar.title = "Profile/${it.login}"
            binding.avatar.load(it.avatar) {
                placeholder(R.drawable.ic_avatar_placeholder)
                error(R.drawable.ic_avatar_placeholder)
            }
        }

        binding.avatar.setOnClickListener {
            if (viewModel.isCurrentUser(userId)) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.choose_image_source)
                    .setItems(arrayOf(getString(R.string.camera), getString(R.string.gallery))) { _, which ->
                        when (which) {
                            0 -> { // Camera
                                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                            1 -> { // Gallery
                                pickPhotoLauncher.launch("image/*")
                            }
                        }
                    }
                    .show()
            }
        }

        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = titles.size

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> UserWallFragment().apply {
                        arguments = Bundle().apply {
                            putLong("userId", userId)
                        }
                    }
                    1 -> UserJobsFragment().apply {
                        arguments = Bundle().apply {
                            putLong("userId", userId)
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid position: $position")
                }
            }
        }

        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
