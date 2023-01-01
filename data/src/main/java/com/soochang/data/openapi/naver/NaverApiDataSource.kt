package com.soochang.data.openapi.naver

import com.soochang.data.openapi.naver.response.NaverBooksResponse
import com.soochang.data.openapi.naver.service.NaverApiService
import javax.inject.Inject

interface NaverApiDataSource {
    suspend fun findBooksByTitle(query: String, currentPage: Int, countPerPage: Int): NaverBooksResponse
    suspend fun findBookByIsbn(isbn: String): NaverBooksResponse.Item
}

class NaverApiDataSourceImpl @Inject constructor(
    private val naverApiService: NaverApiService
    ) : NaverApiDataSource {
    override suspend fun findBooksByTitle(
        query: String,
        currentPage: Int,
        countPerPage: Int
    ): NaverBooksResponse {
        return naverApiService.findBooks(query, currentPage, countPerPage)
    }

    override suspend fun findBookByIsbn(isbn: String): NaverBooksResponse.Item {
        return naverApiService.findBookDetail(isbn)
    }
}