package ru.netology.nework.ui.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.JobItem
import ru.netology.nework.viewmodel.UserJobsViewModel

@AndroidEntryPoint
class UserJobsFragment : FeedFragment() {


    private var isOwner: Boolean = false

    override val viewModel: UserJobsViewModel by viewModels()
    override val adapter by lazy { FeedAdapter(object : OnInteractionListener {
        override fun onEdit(item: DisplayableItem) {
            if (item !is JobItem) return
            findNavController().navigate(
                R.id.action_userJobsFragment_to_newJobFragment,
                Bundle().apply { putLong("jobId", item.job.id) })
        }

        override fun onRemove(item: DisplayableItem) {
            if (item !is JobItem) return
            viewModel.removeById(item.job.id)
        }
    }, isOwner) }

}
