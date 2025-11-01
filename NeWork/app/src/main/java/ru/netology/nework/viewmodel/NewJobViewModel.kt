package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Job
import ru.netology.nework.repository.JobRepository
import javax.inject.Inject
import ru.netology.nework.error.AppError

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
)

@HiltViewModel
class NewJobViewModel @Inject constructor(
    private val repository: JobRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _job = MutableLiveData(empty)
    val edited: LiveData<Job> = _job

    private val _jobCreated = MutableLiveData<Unit>()
    val jobCreated: LiveData<Unit> = _jobCreated

    private val _jobCreationFailed = MutableLiveData<AppError>()
    val jobCreationFailed: LiveData<AppError> = _jobCreationFailed

    init {
        savedStateHandle.get<Long>("jobId")?.let { loadJob(it) }
    }

    fun loadJob(jobId: Long) {
        viewModelScope.launch {
            try {
                val job = repository.getJobById(jobId)
                _job.value = job
            } catch (e: Exception) {
                _jobCreationFailed.value = AppError.from(e)
            }
        }
    }

    fun save() {
        edited.value?.let { job ->
            viewModelScope.launch {
                try {
                    repository.saveJob(job)
                    _jobCreated.value = Unit
                } catch (e: Exception) {
                    _jobCreationFailed.value = AppError.from(e)
                }
            }
        }
    }

    fun changeJob(name: String, position: String, start: String, finish: String?) {
        _job.value = edited.value?.copy(name = name, position = position, start = start, finish = finish)
    }
}
