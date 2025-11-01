package ru.netology.nework.repository

import okhttp3.MultipartBody
import ru.netology.nework.dto.Token

interface UserRepository {
    suspend fun login(login: String, pass: String): Token
    suspend fun register(login: String, pass: String, name: String, avatar: MultipartBody.Part?): Token
    suspend fun getAllUsers(): List<ru.netology.nework.dto.UserResponse>
    suspend fun getUserById(id: Long): ru.netology.nework.dto.UserResponse
}
