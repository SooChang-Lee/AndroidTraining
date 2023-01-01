package com.soochang.domain.usecase.book

import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.repository.openapi.OpenApiRepository

class GetBookListByTitleUseCase(private val openApiRepository: OpenApiRepository) {
    suspend operator fun invoke(
        bookDataSource: OpenApiRepository.BookDataSource,
        query: String,
        currentPage: Int,
        countPerPage: Int
    ): Result<BookItems> {
        val bookItems = openApiRepository.findBooksByTitle(bookDataSource, query, currentPage, countPerPage)
        if( bookItems is Result.Success ){
            bookItems.data.items.map {
                it.query = query
            }
        }

        return bookItems
    }
}