package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.PostRepository
import java.io.File
import javax.inject.Inject
import ru.netology.nework.error.AppError

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
)

private val noMedia = MediaModel()

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val _post = MutableLiveData(empty)
    val edited: LiveData<Post> = _post

    private val _postCreated = MutableLiveData<Unit>()
    val postCreated: LiveData<Unit> = _postCreated

    private val _postCreationFailed = MutableLiveData<AppError>()
    val postCreationFailed: LiveData<AppError> = _postCreationFailed

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel> = _media

    private val _mediaError = MutableLiveData<String?>()
    val mediaError: LiveData<String?> = _mediaError

    fun loadPost(postId: Long) {
        viewModelScope.launch {
            try {
                val post = repository.getById(postId)
                _post.value = post
            } catch (e: Exception) {
                _postCreationFailed.value = AppError.from(e)
            }
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    media.value?.file?.let {
                        val mediaBody = repository.saveMedia(
                            MultipartBody.Part.createFormData("file", it.name, it.asRequestBody())
                        )
                        repository.save(post.copy(attachment = Attachment(mediaBody.url, media.value?.type ?: AttachmentType.IMAGE)))
                    } ?: repository.save(post)

                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _postCreationFailed.value = AppError.from(e)
                }
            }
        }
    }

    fun clearCoordinates() {
        _post.value = edited.value?.copy(coords = null)
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        _post.value = edited.value?.copy(content = text)
    }

    fun changeAuthorJob(authorJob: String) {
        val text = authorJob.trim()
        if (edited.value?.authorJob == text) {
            return
        }
        _post.value = edited.value?.copy(authorJob = text)
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

    fun setCoordinates(lat: Double, long: Double) {
        _post.value = edited.value?.copy(coords = ru.netology.nework.dto.Coordinates(lat, long))
    }

    fun clearMediaError() {
        _mediaError.value = null
    }

    fun setMentioned(userIds: List<Long>?) {
        _post.value = edited.value?.copy(mentionIds = userIds?.toSet() ?: emptySet())
    }
}
