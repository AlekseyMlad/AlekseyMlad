package ru.netology.nework.repository

import okhttp3.MultipartBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.util.processResponse
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun login(login: String, pass: String): Token =
        processResponse { apiService.login(login, pass) }

    override suspend fun register(
        login: String,
        pass: String,
        name: String,
        avatar: MultipartBody.Part?
    ): Token = processResponse { apiService.register(login, pass, name, avatar) }

    override suspend fun getAllUsers(): List<UserResponse> =
        processResponse { apiService.getAllUsers() }

    override suspend fun getUserById(id: Long): UserResponse =
        processResponse { apiService.getUserById(id) }
}
