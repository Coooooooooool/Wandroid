package com.eric.wandroid.data.remote

import com.eric.wandroid.data.remote.dto.ApiResponse
import java.io.IOException

fun <T> ApiResponse<T>.requireData(): T {
    if (errorCode == 0 && data != null) {
        return data
    }
    throw RepositoryException(
        code = errorCode,
        message = when {
            errorCode == -1001 -> "Login expired. Please sign in again."
            errorMsg.isNotBlank() -> errorMsg
            else -> "Server returned empty data."
        }
    )
}

fun ApiResponse<*>.requireSuccess() {
    if (errorCode == 0) return
    throw RepositoryException(
        code = errorCode,
        message = when {
            errorCode == -1001 -> "Login expired. Please sign in again."
            errorMsg.isNotBlank() -> errorMsg
            else -> "Request failed."
        }
    )
}

fun Throwable.apiErrorCode(): Int? = (this as? RepositoryException)?.code

fun Throwable.toRepositoryMessage(): String {
    val repositoryException = this as? RepositoryException
    if (repositoryException != null) {
        return repositoryException.message
    }
    return when (this) {
        is IOException -> "Network request failed. Check your connection and try again."
        else -> message ?: "Request failed. Please try again later."
    }
}

private class RepositoryException(
    val code: Int,
    override val message: String
) : RuntimeException(message)
