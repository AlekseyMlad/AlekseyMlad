package ru.netology.nework.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.PostItem
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.error.AppError
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : BaseViewModel() {

    init {
        observeData(repository.data) { PostItem(it) }
    }

    override fun load() {
        viewModelScope.launch {
            _data.value = _data.value?.copy(loading = true)
            try {
                repository.getAll()
                _data.value = _data.value?.copy(loading = false)
            } catch (e: Exception) {
                _data.value = _data.value?.copy(error = AppError.from(e), loading = false)
            }
        }
    }

    override fun likeById(id: Long) = performUpdate(
        action = { repository.likeById(id) },
        update = { items, likedPost ->
            items.map { if (it is PostItem && it.post.id == id) it.copy(post = likedPost) else it }
        }
    )

    override fun unlikeById(id: Long) = performUpdate(
        action = { repository.unlikeById(id) },
        update = { items, unlikedPost ->
            items.map { if (it is PostItem && it.post.id == id) it.copy(post = unlikedPost) else it }
        }
    )

    override fun removeById(id: Long) = performUpdate(
        action = { repository.removeById(id) },
        update = { items, _ ->
            items.filter { it.id != id }
        }
    )
}
