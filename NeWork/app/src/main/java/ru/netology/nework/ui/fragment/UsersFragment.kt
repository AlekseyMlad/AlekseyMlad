package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.SelectionInteractionListener
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.FabAction
import ru.netology.nework.dto.UserItem
import ru.netology.nework.viewmodel.SharedViewModel
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    private var isSelectionMode = false
    private val selectedUsers = mutableSetOf<Long>()

    private val interactionListener = object : SelectionInteractionListener {
        override fun onItemClick(item: DisplayableItem) {
            if (item !is UserItem) return
            if (isSelectionMode) {
                if (selectedUsers.contains(item.user.id)) {
                    selectedUsers.remove(item.user.id)
                } else {
                    selectedUsers.add(item.user.id)
                }
                adapter.notifyItemChanged(adapter.currentList.indexOf(item))
            } else {
                findNavController().navigate(
                    R.id.action_usersFragment_to_userDetailsFragment,
                    Bundle().apply { putLong("userId", item.user.id) })
            }
        }

        override fun isUserSelected(userId: Long): Boolean = selectedUsers.contains(userId)
    }

    private val adapter: FeedAdapter by lazy { FeedAdapter(interactionListener) }

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it.items)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isSelectionMode = arguments?.getBoolean("isSelectionMode") ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setFabAction(FabAction.NONE)
    }

    override fun onPause() {
        super.onPause()
        sharedViewModel.setFabAction(FabAction.NONE)
    }
}
