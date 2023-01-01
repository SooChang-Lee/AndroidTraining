package com.soochang.domain.usecase.place

import com.soochang.domain.model.Result
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.repository.openapi.OpenApiRepository

class GetDirectionUseCase(private val openApiRepository: OpenApiRepository) {
    suspend operator fun invoke(
        directionDataSource: OpenApiRepository.DirectionDataSource,
        origin: String,
        destination: String
    ): Result<Direction> {
        return openApiRepository.findDirection(
            directionDataSource,
            origin,
            destination
        )
    }
}