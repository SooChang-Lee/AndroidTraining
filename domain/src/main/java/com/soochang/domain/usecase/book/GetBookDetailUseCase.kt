package com.soochang.domain.usecase.book

import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.repository.openapi.OpenApiRepository

class GetBookDetailUseCase(private val openApiRepository: OpenApiRepository) {
    suspend operator fun invoke(
        bookDataSource: OpenApiRepository.BookDataSource,
        volumeId: String
    ): Result<BookItem> {
        return openApiRepository.findBookDetail(bookDataSource, volumeId)
    }
}