package com.soochang.data.openapi.google

import com.soochang.data.openapi.google.response.VolumeListResponse
import com.soochang.data.openapi.google.service.GoogleApiService
import javax.inject.Inject

interface GoogleApiDataSource {
    suspend fun findVolumesByTitle(query: String, currentPage: Int, countPerPage: Int): VolumeListResponse
    suspend fun findVolumeById(volumeId: String): VolumeListResponse.VolumeResponse
}

class GoogleApiDataSourceImpl @Inject constructor(
    private val googleApiService: GoogleApiService
    ) : GoogleApiDataSource {
    override suspend fun findVolumesByTitle(
        query: String,
        currentPage: Int,
        countPerPage: Int
    ): VolumeListResponse {
        return googleApiService.findVolumesByTitle(query, currentPage, countPerPage)
    }

    override suspend fun findVolumeById(volumeId: String): VolumeListResponse.VolumeResponse {
        return googleApiService.findVolumeById(volumeId)
    }
}