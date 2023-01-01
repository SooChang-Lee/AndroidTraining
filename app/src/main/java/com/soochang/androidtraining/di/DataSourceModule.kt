package com.soochang.androidtraining.di

import android.content.Context
import com.soochang.data.openapi.google.GoogleApiDataSource
import com.soochang.data.openapi.google.GoogleApiDataSourceImpl
import com.soochang.data.openapi.kakao.KakaoApiDataSourceImpl
import com.soochang.data.openapi.kakao.KakaoApiDataSource
import com.soochang.data.openapi.naver.NaverApiDataSource
import com.soochang.data.openapi.naver.NaverApiDataSourceImpl
import com.soochang.data.main.local.MainDataSource
import com.soochang.data.main.local.MainDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Singleton
    @Provides
    fun providesMainDataSource(@ApplicationContext appContext: Context): MainDataSource {
        return MainDataSourceImpl(appContext)
    }

    @Singleton
    @Provides
    fun providesGoogleApiDataSource(source: GoogleApiDataSourceImpl): GoogleApiDataSource {
        return source
    }

    @Singleton
    @Provides
    fun providesKakaosApiDataSource(source: KakaoApiDataSourceImpl): KakaoApiDataSource {
        return source
    }

    @Singleton
    @Provides
    fun providesNaverApiDataSource(source: NaverApiDataSourceImpl): NaverApiDataSource {
        return source
    }
}