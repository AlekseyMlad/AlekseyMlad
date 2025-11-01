package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.MentionedUsersAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.viewmodel.EventDetailsViewModel

@AndroidEntryPoint
class SpeakersListFragment : Fragment() {

    private val viewModel: EventDetailsViewModel by activityViewModels()

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)

        val speakerIds = arguments?.getLongArray("speakerIds")?.toList() ?: emptyList()

        val adapter = MentionedUsersAdapter { user ->
            findNavController().navigate(
                R.id.action_global_userDetailsFragment,
                bundleOf("userId" to user.id)
            )
        }
        binding.list.adapter = adapter

        viewModel.event.observe(viewLifecycleOwner) {
            it?.let {
                val speakers = it.users.filter { userEntry -> speakerIds.contains(userEntry.key) }.values.toList()
                adapter.submitList(speakers)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}