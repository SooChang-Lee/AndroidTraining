package com.soochang.domain.usecase.book

//import androidx.paging.PagingData
//import androidx.paging.map
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.repository.openapi.OpenApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//class GetBookListByTitlePagingSourceUseCase(private val openApiRepository: OpenApiRepository) {
//    suspend operator fun invoke(
//        bookDataSource: OpenApiRepository.BookDataSource,
//        query: String,
//        countPerPage: Int
//    ): Flow<PagingData<BookItem>> {
//        return openApiRepository.findBooksByTitlePagingSource(bookDataSource, query, countPerPage).map { pagingData ->
//            pagingData.map {
//                it.query = query
//                it
//            }
//        }
//    }
//}