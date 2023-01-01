package com.soochang.data.openapi.kakao.service

import com.soochang.domain.model.direction.Direction
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoMobilityApiService {
    @GET("/v1/directions")
    suspend fun findDirection(
        @Query("origin") origin: String,//출발지
        @Query("destination") destination: String,//출발지
        @Query("priority") priority: String = "RECOMMEND",//RECOMMEND=추천경로, TIME=최단시간, DISTANCE=최단경로
    ) : Direction
}