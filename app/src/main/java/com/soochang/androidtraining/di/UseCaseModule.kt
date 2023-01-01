package com.soochang.androidtraining.di

import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.domain.usecase.book.GetBookDetailUseCase
import com.soochang.domain.usecase.book.GetBookListByTitleUseCase
import com.soochang.domain.usecase.place.GetDirectionUseCase
import com.soochang.domain.usecase.place.GetPlaceListByCategoryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun providesGetApiListByTitleUseCase(openApiRepository: OpenApiRepository): GetBookListByTitleUseCase {
        return GetBookListByTitleUseCase(openApiRepository)
    }

    @Provides
    fun providesGetApiDetailUseCase(repository: OpenApiRepository): GetBookDetailUseCase {
        return GetBookDetailUseCase(repository)
    }

    @Provides
    fun providesGetPlaceListByCategoryUseCase(repository: OpenApiRepository): GetPlaceListByCategoryUseCase {
        return GetPlaceListByCategoryUseCase(repository)
    }

    @Provides
    fun providesGetDirectionUseCase(repository: OpenApiRepository): GetDirectionUseCase {
        return GetDirectionUseCase(repository)
    }
}