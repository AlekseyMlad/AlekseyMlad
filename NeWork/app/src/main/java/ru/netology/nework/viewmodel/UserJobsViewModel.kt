package ru.netology.nework.viewmodel

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.repository.JobRepository
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nework.error.AppError

@HiltViewModel
class UserJobsViewModel @Inject constructor(
    private val repository: JobRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    

    private val userId = savedStateHandle.get<Long>("userId") ?: 0L

    override fun load() = loadData(
        loader = { repository.getJobs(userId) },
        mapper = { ru.netology.nework.dto.JobItem(it) }
    )

        override fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeJobById(id)
                load()
            } catch (e: Exception) {
                _data.value = _data.value?.copy(error = AppError.from(e))
            }
        }
    }

    override fun likeById(id: Long) {}

    override fun unlikeById(id: Long) {}
}
