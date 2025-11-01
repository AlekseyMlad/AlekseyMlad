package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject

import ru.netology.nework.error.AppError

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val repository: PostRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _error = MutableLiveData<AppError>()
    val error: LiveData<AppError> = _error

    init {
        loadPost()
    }

    fun loadPost() {
        viewModelScope.launch {
            val postId = savedStateHandle.get<Long>("postId") ?: return@launch
            try {
                val post = repository.getById(postId)
                _post.value = post
            } catch (e: Exception) {
                _error.value = AppError.from(e)
            }
        }
    }
}
