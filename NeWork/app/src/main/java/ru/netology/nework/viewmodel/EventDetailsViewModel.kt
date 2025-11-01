package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Event
import ru.netology.nework.repository.EventRepository
import javax.inject.Inject

import ru.netology.nework.error.AppError

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _error = MutableLiveData<AppError>()
    val error: LiveData<AppError> = _error

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            val eventId = savedStateHandle.get<Long>("eventId") ?: return@launch
            try {
                val event = repository.getById(eventId)
                _event.value = event
            } catch (e: Exception) {
                _error.value = AppError.from(e)
            }
        }
    }
}
