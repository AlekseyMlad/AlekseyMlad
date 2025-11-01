package ru.netology.nework.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.repository.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : BaseViewModel() {

    override fun load() = loadData(
        loader = repository::getAll,
        mapper = { ru.netology.nework.dto.EventItem(it) }
    )

    override fun likeById(id: Long) = performUpdate(
        action = { repository.likeById(id) },
        update = { items, likedEvent ->
            items.map { if (it is ru.netology.nework.dto.EventItem && it.event.id == id) it.copy(event = likedEvent) else it }
        }
    )

    override fun unlikeById(id: Long) = performUpdate(
        action = { repository.unlikeById(id) },
        update = { items, unlikedEvent ->
            items.map { if (it is ru.netology.nework.dto.EventItem && it.event.id == id) it.copy(event = unlikedEvent) else it }
        }
    )

    override fun removeById(id: Long) = performUpdate(
        action = { repository.removeById(id) },
        update = { items, _ ->
            items.filter { it.id != id }
        }
    )

    fun participate(id: Long) = performUpdate(
        action = { repository.participate(id) },
        update = { items, participatedEvent ->
            items.map { if (it is ru.netology.nework.dto.EventItem && it.event.id == id) it.copy(event = participatedEvent) else it }
        }
    )

    fun unparticipate(id: Long) = performUpdate(
        action = { repository.unparticipate(id) },
        update = { items, unparticipatedEvent ->
            items.map { if (it is ru.netology.nework.dto.EventItem && it.event.id == id) it.copy(event = unparticipatedEvent) else it }
        }
    )
}
