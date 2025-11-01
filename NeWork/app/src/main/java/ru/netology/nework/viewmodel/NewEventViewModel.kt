package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.EventRepository
import java.io.File
import javax.inject.Inject
import ru.netology.nework.error.AppError

import ru.netology.nework.repository.PostRepository

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = ru.netology.nework.dto.EventType.ONLINE,
)

private val noMedia = MediaModel()

@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _event = MutableLiveData(empty)
    val edited: LiveData<Event> = _event

    private val _eventCreated = MutableLiveData<Unit>()
    val eventCreated: LiveData<Unit> = _eventCreated

    private val _eventCreationFailed = MutableLiveData<AppError>()
    val eventCreationFailed: LiveData<AppError> = _eventCreationFailed

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel> = _media

    private val _mediaError = MutableLiveData<String?>()
    val mediaError: LiveData<String?> = _mediaError

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getById(eventId)
                _event.value = event
            } catch (e: Exception) {
                _eventCreationFailed.value = AppError.from(e)
            }
        }
    }

    fun save() {
        edited.value?.let { event ->
            viewModelScope.launch {
                try {
                    media.value?.file?.let {
                        val mediaBody = postRepository.saveMedia(
                            okhttp3.MultipartBody.Part.createFormData("file", it.name, it.asRequestBody())
                        )
                        eventRepository.save(event.copy(attachment = ru.netology.nework.dto.Attachment(mediaBody.url, media.value?.type ?: AttachmentType.IMAGE)))
                    } ?: eventRepository.save(event)

                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    _eventCreationFailed.value = AppError.from(e)
                }
            }
        }
    }

    fun clearCoordinates() {
        _event.value = edited.value?.copy(coords = null)
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        _event.value = edited.value?.copy(content = text)
    }

    fun changeAuthorJob(authorJob: String) {
        val text = authorJob.trim()
        if (edited.value?.authorJob == text) {
            return
        }
        _event.value = edited.value?.copy(authorJob = text)
    }

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        if (file != null && file.length() > 15 * 1024 * 1024) {
            _mediaError.value = "File size exceeds 15MB"
            return
        }
        _media.value = MediaModel(uri, file, type)
    }

    fun clearMedia() {
        _media.value = noMedia
    }

    fun clearMediaError() {
        _mediaError.value = null
    }

    fun setDatetime(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(year, month, dayOfMonth, hourOfDay, minute)
        }
        val formatted = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(calendar.time)
        _event.value = edited.value?.copy(datetime = formatted)
    }

    fun setType(type: ru.netology.nework.dto.EventType?) {
        type?.let {
            _event.value = edited.value?.copy(type = it)
        }
    }

    fun setSpeakers(userIds: List<Long>?) {
        _event.value = edited.value?.copy(speakerIds = userIds?.toSet() ?: emptySet())
    }

    fun setCoordinates(lat: Double, long: Double) {
        _event.value = edited.value?.copy(coords = ru.netology.nework.dto.Coordinates(lat, long))
    }
}
