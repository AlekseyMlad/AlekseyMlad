package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.util.toFile
import ru.netology.nework.viewmodel.NewEventViewModel

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: NewEventViewModel by viewModels()

    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)

        val eventId = arguments?.getLong("eventId")
        if (eventId != null && eventId != 0L) {
            viewModel.loadEvent(eventId)
        }

        viewModel.edited.observe(viewLifecycleOwner) {
            if (binding.eventText.text.toString() != it.content) {
                binding.eventText.setText(it.content)
            }
            binding.eventText.setText(it.content)
            binding.authorJob.setText(it.authorJob)
            if (it.type == ru.netology.nework.dto.EventType.ONLINE) {
                binding.typeGroup.check(R.id.online_button)
            } else {
                binding.typeGroup.check(R.id.offline_button)
            }
        }

        binding.eventText.addTextChangedListener(object : TextWatcher {
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

        requireActivity().addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.new_item_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean = when (menuItem.itemId) {
                R.id.save -> {
                    val content = binding.eventText.text.toString()
                    val datetime = viewModel.edited.value?.datetime
                    if (content.isBlank() || datetime.isNullOrBlank()) {
                        Snackbar.make(binding.root, "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
                    } else {
                        viewModel.save()
                    }
                    true
                }

                else -> false
            }
        }, viewLifecycleOwner)

        binding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.online_button -> ru.netology.nework.dto.EventType.ONLINE
                R.id.offline_button -> ru.netology.nework.dto.EventType.OFFLINE
                else -> null
            }
            viewModel.setType(type)
        }

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
                    findNavController().navigate(R.id.action_newEventFragment_to_mapPickerFragment)
                    true
                }

                R.id.action_datetime -> {
                    val calendar = java.util.Calendar.getInstance()
                    val datePickerDialog = android.app.DatePickerDialog(
                        requireContext(),
                        { _, year, month, dayOfMonth ->
                            val timePickerDialog = android.app.TimePickerDialog(
                                requireContext(),
                                { _, hourOfDay, minute ->
                                    viewModel.setDatetime(year, month, dayOfMonth, hourOfDay, minute)
                                },
                                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                calendar.get(java.util.Calendar.MINUTE),
                                true
                            )
                            timePickerDialog.show()
                        },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.show()
                    true
                }

                R.id.action_speakers -> {
                    val bundle = Bundle().apply { putBoolean("isSelectionMode", true) }
                    findNavController().navigate(R.id.usersFragment, bundle)
                    true
                }

                else -> false
            }
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.eventCreationFailed.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, "Failed to create event", Snackbar.LENGTH_SHORT).show()
        }

        viewModel.edited.observe(viewLifecycleOwner) { event ->
            binding.map.isVisible = event.coords != null
            if (event.coords != null) {
                binding.map.mapWindow.map.mapObjects.clear()
                binding.map.mapWindow.map.move(
                    CameraPosition(
                        Point(event.coords.lat, event.coords.long),
                        15.0f,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 5f),
                    null
                )
                binding.map.mapWindow.map.mapObjects.addPlacemark(
                    Point(event.coords.lat, event.coords.long),
                    ImageProvider.fromResource(requireContext(), R.drawable.ic_location_on)
                )
            }
        }

        binding.clearLocationButton.setOnClickListener {
            viewModel.clearCoordinates()
            binding.map.visibility = View.GONE
        }

        setFragmentResultListener("map_picker_result") { _, bundle ->
            val lat = bundle.getDouble("lat")
            val long = bundle.getDouble("long")
            viewModel.setCoordinates(lat, long)
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
