package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post): Post
    suspend fun getById(id: Long): Post
    suspend fun saveMedia(media: MultipartBody.Part): Media
    suspend fun removeById(id: Long)
    suspend fun getWall(userId: Long): List<Post>
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
}
