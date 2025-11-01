package ru.netology.nework.viewmodel

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class UserWallViewModel @Inject constructor(
    private val repository: PostRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val userId = savedStateHandle.get<Long>("userId") ?: 0L

    override fun load() = loadData(
        loader = { repository.getWall(userId) },
        mapper = { ru.netology.nework.dto.PostItem(it) }
    )

    override fun likeById(id: Long) = performUpdate(
        action = { repository.likeById(id) },
        update = { items, likedPost ->
            items.map { if (it is ru.netology.nework.dto.PostItem && it.post.id == id) it.copy(post = likedPost) else it }
        }
    )

    override fun unlikeById(id: Long) = performUpdate(
        action = { repository.unlikeById(id) },
        update = { items, unlikedPost ->
            items.map { if (it is ru.netology.nework.dto.PostItem && it.post.id == id) it.copy(post = unlikedPost) else it }
        }
    )

    override fun removeById(id: Long) = performUpdate(
        action = { repository.removeById(id) },
        update = { items, _ ->
            items.filter { it.id != id }
        }
    )
}