package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Event
import ru.netology.nework.util.processEmptyResponse
import ru.netology.nework.util.processResponse
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : EventRepository {

    override suspend fun getAll(): List<Event> = processResponse { apiService.getAllEvents() }

    override suspend fun save(event: Event): Event = processResponse { apiService.saveEvent(event) }

    override suspend fun getById(id: Long): Event = processResponse { apiService.getEventById(id) }

    override suspend fun removeById(id: Long) =
        processEmptyResponse { apiService.removeEventById(id) }

    override suspend fun likeById(id: Long): Event =
        processResponse { apiService.likeEventById(id) }

    override suspend fun unlikeById(id: Long): Event =
        processResponse { apiService.unlikeEventById(id) }

    override suspend fun participate(id: Long): Event =
        processResponse { apiService.participateInEvent(id) }

    override suspend fun unparticipate(id: Long): Event =
        processResponse { apiService.unparticipateInEvent(id) }
}
