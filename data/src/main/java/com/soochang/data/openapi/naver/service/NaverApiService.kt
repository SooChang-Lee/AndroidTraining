package com.soochang.data.openapi.naver.service

import com.soochang.data.openapi.naver.response.NaverBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverApiService {
    @GET("/v1/search/book.json")
    suspend fun findBooks(
        @Query("query") query: String,
        @Query("start") start: Int = 1,
        @Query("display") display: Int = 30
    ) : NaverBooksResponse

    @GET("/v1/search/book_adv.json")
    suspend fun findBookDetail(
        @Query("d_isbn") isbn: String
    ) : NaverBooksResponse.Item
}