package ru.netology.nework.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentSignUpBinding
import ru.netology.nework.viewmodel.SignUpViewModel
import ru.netology.nework.util.toFile

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Snackbar.make(binding.root, ImagePicker.getError(it.data), Snackbar.LENGTH_LONG).show()
                }
                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    viewModel.setAvatar(uri!!, uri.toFile(requireContext()))
                }
            }
        }

        binding.avatar.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .createIntent(imagePicker::launch)
        }

        binding.signUpButton.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val name = binding.name.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()

            if (pass != confirmPass) {
                binding.confirmPasswordLayout.error = "Passwords do not match"
                return@setOnClickListener
            }

            viewModel.register(login, pass, name)
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.loading
            binding.signUpButton.isEnabled = !state.loading

            if (state.error) {
                Snackbar.make(binding.root, "Registration error", Snackbar.LENGTH_LONG).show()
            }

            if (state.success) {
                findNavController().navigateUp()
            }
        }

        viewModel.avatar.observe(viewLifecycleOwner) { avatar ->
            binding.avatar.setImageURI(avatar.uri)
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { message ->
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
}
