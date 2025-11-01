package ru.netology.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.repository.UserRepositoryImpl
import ru.netology.nework.repository.PostRepositoryImpl
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.EventRepositoryImpl
import ru.netology.nework.repository.PostRepository
import javax.inject.Singleton

import ru.netology.nework.repository.JobRepository
import ru.netology.nework.repository.JobRepositoryImpl

@Suppress("unused")
@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Singleton
    @Binds
    fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Singleton
    @Binds
    fun bindJobRepository(impl: JobRepositoryImpl): JobRepository
}
