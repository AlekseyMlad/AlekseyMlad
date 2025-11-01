package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class LikersListFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)

        val likerIds = arguments?.getLongArray("likerIds")?.toList() ?: emptyList()

        val adapter = UsersAdapter { user ->
            findNavController().navigate(
                ru.netology.nework.R.id.action_global_userDetailsFragment,
                Bundle().apply { putLong("userId", user.id) }
            )
        }
        binding.list.adapter = adapter

//        userViewModel.data.observe(viewLifecycleOwner) {
//            it?.let {
//                val likes = it.users.filter { userRes: UserPreview -> userPreview.id in likerIds }
//                adapter.submitList(likes)
//            }
//        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
