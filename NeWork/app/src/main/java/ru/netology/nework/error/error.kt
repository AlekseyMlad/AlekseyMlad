package ru.netology.nework.error

import retrofit2.HttpException
import java.io.IOException
import java.sql.SQLException

sealed class AppError(val code: String) : RuntimeException() {
    class ApiError(val status: Int, code: String) : AppError(code)
    object Network : AppError("error_network") {
        private fun readResolve(): Any = Network
    }
    object Db : AppError("error_db") {
        private fun readResolve(): Any = Db
    }
    object Unknown : AppError("error_unknown") {
        private fun readResolve(): Any = Unknown
    }



    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> Db
            is IOException -> Network
            is HttpException -> {
                val code = e.code()
                ApiError(code, "error_api_$code")
            }
            else -> Unknown
        }
    }
}