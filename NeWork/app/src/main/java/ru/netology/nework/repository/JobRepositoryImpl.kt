package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Job
import ru.netology.nework.util.processEmptyResponse
import ru.netology.nework.util.processResponse
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : JobRepository {
    override suspend fun getJobs(userId: Long): List<Job> =
        processResponse { apiService.getJobs(userId) }

    override suspend fun saveJob(job: Job): Job = processResponse { apiService.saveJob(job) }

    override suspend fun removeJobById(id: Long) =
        processEmptyResponse { apiService.removeJobById(id) }

    override suspend fun getJobById(id: Long): Job = processResponse { apiService.getJobById(id) }
}
