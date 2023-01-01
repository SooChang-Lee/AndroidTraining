package com.soochang.domain.model.error

/**
 * ErrorEntity
 * data layer에서 발생하는 에러 유형을 미리정의
 * Api, Local DB, File등에서 발생하는 에러 관리
 */
sealed class ErrorEntity: Throwable() {
    sealed class Api {
        //네트워크 단절
        data class Network(val throwable: Throwable? = null) : ErrorEntity()

        //시스템오류
        data class ServiceUnvailable(val throwable: Throwable? = null) : ErrorEntity()

        //리소스가 존재하지 않음
        data class NotFound(val throwable: Throwable? = null) : ErrorEntity()

        //기타 알 수 없는 에러
        data class UnknownError(val throwable: Throwable? = null) : ErrorEntity()
    }

    sealed class Db(): ErrorEntity() {
        //알 수 없는 에러
        data class UnknownError(val throwable: Throwable? = null) : ErrorEntity()
    }

    sealed class File(): ErrorEntity() {
        //파일이 존재하지 않음
        data class NotFound(val throwable: Throwable? = null) : ErrorEntity()

        //알 수 없는 에러
        data class UnknownError(val throwable: Throwable? = null) : ErrorEntity()
    }
}