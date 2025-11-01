package ru.netology.nework.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : BaseViewModel() {

    init {
        load()
    }

    override fun load() = loadData(
        loader = repository::getAllUsers,
        mapper = { ru.netology.nework.dto.UserItem(it) }
    )

    override fun likeById(id: Long) {}

    override fun unlikeById(id: Long) {}

    override fun removeById(id: Long) {}
}