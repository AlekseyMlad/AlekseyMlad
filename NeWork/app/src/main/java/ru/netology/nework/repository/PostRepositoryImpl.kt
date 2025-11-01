package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.AppError
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
) : PostRepository {

    override val data: Flow<List<Post>> = postDao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = apiService.getAllPosts()
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw AppError.ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun save(post: Post): Post {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw AppError.ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun getById(id: Long): Post {
        // This method is not used in the main feed, so we don't need to cache it for now.
        try {
            val response = apiService.getPostById(id)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            return response.body() ?: throw AppError.ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun saveMedia(media: MultipartBody.Part): Media {
        try {
            val response = apiService.saveMedia(media)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            return response.body() ?: throw AppError.ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = apiService.removePostById(id)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun getWall(userId: Long): List<Post> {
        // This method is not used in the main feed, so we don't need to cache it for now.
        try {
            val response = apiService.getWall(userId)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            return response.body() ?: throw AppError.ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun likeById(id: Long): Post {
        try {
            val response = apiService.likePostById(id)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw AppError.ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }

    override suspend fun unlikeById(id: Long): Post {
        try {
            val response = apiService.unlikePostById(id)
            if (!response.isSuccessful) {
                throw AppError.ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw AppError.ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw AppError.Network
        } catch (e: Exception) {
            throw AppError.Unknown
        }
    }
}