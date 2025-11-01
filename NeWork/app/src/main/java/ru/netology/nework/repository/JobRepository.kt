package ru.netology.nework.repository

import ru.netology.nework.dto.Job

interface JobRepository {
    suspend fun getJobs(userId: Long): List<Job>
    suspend fun saveJob(job: Job): Job
    suspend fun removeJobById(id: Long)
    suspend fun getJobById(id: Long): Job
}
