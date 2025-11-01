package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.*

interface ApiService {
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removePostById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikePostById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun unlikeEventById(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateInEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun unparticipateInEvent(@Path("id") id: Long): Response<Event>

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(@Field("login") login: String, @Field("pass") pass: String): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part media: MultipartBody.Part?
    ): Response<Token>

    @Multipart
    @POST("media")
    suspend fun saveMedia(@Part media: MultipartBody.Part): Response<Media>

    @GET("{authorId}/wall")
    suspend fun getWall(@Path("authorId") authorId: Long): Response<List<Post>>

    @GET("{userId}/jobs")
    suspend fun getJobs(@Path("userId") userId: Long): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    @GET("my/jobs/{id}")
    suspend fun getJobById(@Path("id") id: Long): Response<Job>
}
