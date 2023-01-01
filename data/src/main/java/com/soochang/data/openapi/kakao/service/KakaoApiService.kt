package com.soochang.data.openapi.kakao.service

import com.soochang.data.openapi.kakao.response.KakaoBooksResponse
import com.soochang.data.openapi.kakao.response.KakaoPlaceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoApiService {
    @GET("v3/search/book")
    suspend fun findBooks(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 30,
        @Query("target") kakaoBooksSearchTarget: String? = KakaoBooksSearchTarget.TITLE.code
    ) : KakaoBooksResponse

    enum class KakaoBooksSearchTarget(val code : String) {
        TITLE("title"),
        ISBN("ISBN"),
        PUBLISHER("publisher"),
        PERSON("person")
    }

    @GET("/v2/local/search/category.json")
    suspend fun findPlaceByCategory(
        @Query("category_group_code") locationCategory: String,
        @Query("rect") rect: String,
        @Query("page") currentPage: Int,
        @Query("size") countPerPage: Int,
    ) : KakaoPlaceResponse
}