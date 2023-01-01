package com.soochang.data.error

import com.soochang.domain.model.error.ErrorEntity
import com.soochang.domain.model.error.ErrorHandler
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
* ApiErrorHandlerImpl
* 발생한 Exception에 해당하는 에러 유형을 정의된 타입으로 반환
*/
class ApiErrorHandler @Inject constructor() : ErrorHandler {
    override fun getErrorEntity(throwable: Throwable): ErrorEntity {
        return when(throwable) {
            is IOException -> ErrorEntity.Api.Network(throwable)
            is HttpException -> {
                when(throwable.code()) {
                    HttpURLConnection.HTTP_NOT_FOUND -> ErrorEntity.Api.NotFound(throwable)

                    HttpURLConnection.HTTP_BAD_REQUEST,
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_FORBIDDEN,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    HttpURLConnection.HTTP_BAD_GATEWAY,
                    HttpURLConnection.HTTP_UNAVAILABLE -> ErrorEntity.Api.ServiceUnvailable(throwable)

                    else -> ErrorEntity.Api.UnknownError(throwable)
                }
            }
            else -> ErrorEntity.Api.UnknownError(throwable)
        }
    }
}

class DbErrorHandler @Inject constructor() : ErrorHandler {
    override fun getErrorEntity(throwable: Throwable): ErrorEntity {
        return ErrorEntity.Api.UnknownError(throwable)
    }
}