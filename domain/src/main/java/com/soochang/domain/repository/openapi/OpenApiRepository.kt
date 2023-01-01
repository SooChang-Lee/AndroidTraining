package com.soochang.domain.repository.openapi

import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItems

interface OpenApiRepository {
    enum class BookDataSource{
        GoogleBooks,
        KakaoBooks,
        NaverBooks
    }

    enum class PlaceDataSource{
        KakaoPlace
    }

    enum class DirectionDataSource{
        KakaoMobility,
        Naver,
        Tmap
    }

    suspend fun findBooksByTitle(bookDataSource: BookDataSource, query: String, currentPage: Int = 1, countPerPage: Int = 30): Result<BookItems>
//    suspend fun findBooksByTitlePagingSource(bookDataSource: BookDataSource, query: String, countPerPage: Int = 30): Flow<PagingData<BookItem>>

    suspend fun findBookDetail(bookDataSource: BookDataSource, id: String): Result<BookItem>

    suspend fun findPlaceByCategory(placeDataSource: PlaceDataSource, placeCategory: PlaceCategory, rect: String, currentPage: Int = 1, countPerPage: Int = 30): Result<PlaceItems>

    suspend fun findDirection(directionDataSource: DirectionDataSource, origin: String, destination: String): Result<Direction>
}