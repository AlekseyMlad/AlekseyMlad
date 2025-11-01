package ru.netology.nework.ui.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.adapter.FeedAdapter
import ru.netology.nework.adapter.OnInteractionListener
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.dto.DisplayableItem
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.FabAction
import ru.netology.nework.viewmodel.SharedViewModel

@AndroidEntryPoint
class EventsFragment : FeedFragment() {
    override val viewModel: EventViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    override val adapter by lazy { FeedAdapter(object : OnInteractionListener {
        override fun onEdit(item: DisplayableItem) {
            if (item !is EventItem) return
            findNavController().navigate(
                R.id.action_eventsFragment_to_newEventFragment,
                Bundle().apply { putLong("eventId", item.event.id) })
        }

        override fun onRemove(item: DisplayableItem) {
            if (item !is EventItem) return
            viewModel.removeById(item.event.id)
        }

        override fun onItemClick(item: DisplayableItem) {
            if (item !is EventItem) return
            findNavController().navigate(
                R.id.action_eventsFragment_to_eventDetailsFragment,
                Bundle().apply { putLong("eventId", item.event.id) })
        }

        override fun onLike(item: DisplayableItem) {
            if (item !is EventItem) return
            if (item.event.likedByMe) {
                viewModel.unlikeById(item.event.id)
            } else {
                viewModel.likeById(item.event.id)
            }
        }

        override fun onParticipate(item: DisplayableItem) {
            if (item !is EventItem) return
            if (item.event.participatedByMe) {
                viewModel.unparticipate(item.event.id)
            } else {
                viewModel.participate(item.event.id)
            }
        }
    }) }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setFabAction(FabAction.NEW_EVENT)
    }

    override fun onPause() {
        super.onPause()
        sharedViewModel.setFabAction(FabAction.NONE)
    }
}
