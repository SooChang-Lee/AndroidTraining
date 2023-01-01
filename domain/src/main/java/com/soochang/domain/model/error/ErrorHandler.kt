package com.soochang.domain.model.error

interface ErrorHandler {
    fun getErrorEntity(throwable: Throwable): ErrorEntity
}