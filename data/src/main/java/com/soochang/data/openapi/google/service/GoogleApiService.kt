package com.soochang.data.openapi.google.service

import com.soochang.data.openapi.google.response.VolumeListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleApiService {
    @GET("/books/v1/volumes")
    suspend fun findVolumesByTitle(
        @Query("q") query: String,
        @Query("startIndex") currentPage: Int = 1,
        @Query("maxResults") countPerPage: Int = 30
    ) : VolumeListResponse

    @GET("/books/v1/volumes/{volumeId}")
    suspend fun findVolumeById(
        @Path("volumeId") volumeId: String
    ) : VolumeListResponse.VolumeResponse
}