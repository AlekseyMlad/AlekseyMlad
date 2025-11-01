package ru.netology.nework.repository

import ru.netology.nework.dto.Event

interface EventRepository {
    suspend fun getAll(): List<Event>
    suspend fun save(event: Event): Event
    suspend fun getById(id: Long): Event
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long): Event
    suspend fun unlikeById(id: Long): Event
    suspend fun participate(id: Long): Event
    suspend fun unparticipate(id: Long): Event
}
