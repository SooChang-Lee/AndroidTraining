package com.soochang.data.openapi.kakao

import com.soochang.data.openapi.kakao.response.KakaoBooksResponse
import com.soochang.data.openapi.kakao.response.KakaoBooksResponse.Document
import com.soochang.data.openapi.kakao.response.KakaoDirectionsResponse
import com.soochang.data.openapi.kakao.response.KakaoPlaceResponse
import com.soochang.data.openapi.kakao.service.KakaoApiService
import com.soochang.data.openapi.kakao.service.KakaoMobilityApiService
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import javax.inject.Inject

interface KakaoApiDataSource {
    suspend fun findBooksByTitle(query: String, currentPage: Int, countPerPage: Int): KakaoBooksResponse
    suspend fun findBookByIsbn(isbn: String): Document

    suspend fun findPlaceByCategory(placeCategory: PlaceCategory, currentPage: Int, countPerPage: Int, rect: String): KakaoPlaceResponse

    suspend fun findDirection(origin: String, destination: String): Direction
}

class KakaoApiDataSourceImpl @Inject constructor(
    private val kakaoApiService: KakaoApiService,
    private val kakaoMobilityApiService: KakaoMobilityApiService
    ) : KakaoApiDataSource {

    override suspend fun findBooksByTitle(query: String, currentPage: Int, countPerPage: Int): KakaoBooksResponse {
        return kakaoApiService.findBooks(query, currentPage, countPerPage, KakaoApiService.KakaoBooksSearchTarget.TITLE.code)
    }

    override suspend fun findBookByIsbn(isbn: String): Document {
        return kakaoApiService.findBooks(isbn, 1, 10, KakaoApiService.KakaoBooksSearchTarget.ISBN.code).documents.first()
    }

    override suspend fun findPlaceByCategory(placeCategory: PlaceCategory, currentPage: Int, countPerPage: Int, rect: String): KakaoPlaceResponse {
        return kakaoApiService.findPlaceByCategory(placeCategory.id, rect, currentPage, countPerPage)
    }

    override suspend fun findDirection(origin: String, destination: String): Direction {
        return kakaoMobilityApiService.findDirection(origin, destination)

    }
}