package com.soochang.domain.usecase.place

import com.soochang.domain.model.Result
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItems
import com.soochang.domain.repository.openapi.OpenApiRepository

class GetPlaceListByCategoryUseCase(private val openApiRepository: OpenApiRepository) {
    suspend operator fun invoke(
        placeDataSource: OpenApiRepository.PlaceDataSource,
        placeCategory: PlaceCategory,
        rect: String,
        currentPage: Int,
        countPerPage: Int
    ): Result<PlaceItems> {
        return openApiRepository.findPlaceByCategory(
            placeDataSource,
            placeCategory,
            rect,
            currentPage,
            countPerPage
        )
    }
}