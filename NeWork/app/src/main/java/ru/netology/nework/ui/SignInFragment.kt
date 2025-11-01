package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentSignInBinding
import ru.netology.nework.viewmodel.SignInViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.signInButton.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            viewModel.login(login, pass)
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.loading
            binding.signInButton.isEnabled = !state.loading

            if (state.error) {
                Snackbar.make(binding.root, "Authentication error", Snackbar.LENGTH_LONG).show()
            }

            if (state.success) {
                val postLoginDestination = arguments?.getInt("postLoginDestination", 0) ?: 0
                if (postLoginDestination != 0) {
                    findNavController().navigate(postLoginDestination)
                } else {
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { message ->
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
}
