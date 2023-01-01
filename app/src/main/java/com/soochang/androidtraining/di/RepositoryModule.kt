package com.soochang.androidtraining.di

import com.soochang.data.openapi.OpenApiRepositoryImpl
import com.soochang.data.main.MainRepositoryImpl
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.domain.repository.main.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun providesMainRepository(repository: MainRepositoryImpl): MainRepository {
        return repository
    }

    @Singleton
    @Provides
    fun providesOpenRepository(repository: OpenApiRepositoryImpl): OpenApiRepository {
        return repository
    }
}