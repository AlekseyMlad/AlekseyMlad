package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentChooserBinding

class ChooserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChooserBinding.inflate(inflater, container, false)

        binding.newPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooserFragment_to_newPostFragment)
        }

        binding.newEventButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooserFragment_to_newEventFragment)
        }

        return binding.root
    }
}