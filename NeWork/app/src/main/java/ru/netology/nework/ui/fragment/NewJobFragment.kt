package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.viewmodel.NewJobViewModel

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val viewModel: NewJobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)

        viewModel.edited.observe(viewLifecycleOwner) {
            binding.company.setText(it.name)
            binding.position.setText(it.position)
            binding.start.setText(it.start)
            binding.end.setText(it.finish)
        }

        binding.saveButton.setOnClickListener {
            viewModel.changeJob(
                binding.company.text.toString(),
                binding.position.text.toString(),
                binding.start.text.toString(),
                binding.end.text.toString().takeIf { it.isNotBlank() }
            )
            viewModel.save()
        }

        viewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.jobCreationFailed.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, "Failed to create job", Snackbar.LENGTH_SHORT).show()
        }

        return binding.root
    }
}
