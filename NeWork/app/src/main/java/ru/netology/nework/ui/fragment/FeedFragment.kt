package ru.netology.nework.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.viewmodel.BaseViewModel
import ru.netology.nework.R

abstract class FeedFragment : Fragment() {

    protected abstract val viewModel: BaseViewModel


    protected abstract val adapter: FeedAdapter

    private var _binding: FragmentFeedBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        binding.list.adapter = adapter

        viewModel.errorEvent.observe(viewLifecycleOwner) { error ->
            Snackbar.make(binding.root, error.message ?: getString(R.string.error_loading_failed), Snackbar.LENGTH_LONG).show()
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.items)
            binding.progressBar.isVisible = state.loading
            binding.emptyText.isVisible = state.empty
        }

        binding.retryButton.setOnClickListener {
            viewModel.load()
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.load()
            binding.swiperefresh.isRefreshing = false
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

