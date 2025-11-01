package ru.netology.nework.util

import retrofit2.HttpException
import retrofit2.Response
import ru.netology.nework.error.AppError

suspend fun <T> processResponse(call: suspend () -> Response<T>): T {
    try {
        val response = call()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response.body() ?: throw RuntimeException("Response body is null")
    } catch (e: Exception) {
        throw AppError.from(e)
    }
}

suspend fun processEmptyResponse(call: suspend () -> Response<Unit>) {
    try {
        val response = call()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    } catch (e: Exception) {
        throw AppError.from(e)
    }
}
