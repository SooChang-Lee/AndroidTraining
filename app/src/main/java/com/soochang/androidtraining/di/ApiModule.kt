package com.soochang.androidtraining.di

import android.content.Context
import com.soochang.data.BuildConfig
import com.soochang.data.openapi.google.service.GoogleApiService
import com.soochang.data.openapi.kakao.service.KakaoApiService
import com.soochang.data.openapi.kakao.service.KakaoMobilityApiService
import com.soochang.data.openapi.naver.service.NaverApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {
    private const val CONNECT_TIMEOUT = 15L
    private const val WRITE_TIMEOUT = 15L
    private const val READ_TIMEOUT = 15L

    @Provides
    @Singleton
    fun providekOkHttpClientCache(@ApplicationContext application: Context): Cache = Cache(application.cacheDir, 10L * 1024 * 1024)

    @Provides
    @Singleton
    fun providekLoggingInterceptor(): HttpLoggingInterceptor{
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Google Api
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GoogleOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class GoogleRetrofit

    @Provides
    @Singleton
    @GoogleOkHttpClient
    fun provideGoogleOkHttpClient(cache: Cache, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient{
        return OkHttpClient.Builder().apply {
            cache(cache)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor {
                val url: HttpUrl = it.request().url.newBuilder().addQueryParameter("key", BuildConfig.GOOGLE_API_KEY).build()

                val request = it.request().newBuilder().url(url).build()

                it.proceed(request)
            }
            addInterceptor(httpLoggingInterceptor)
        }.build()
    }

    @Provides
    @Singleton
    @GoogleRetrofit
    fun provideGoogleRetrofit(@GoogleOkHttpClient client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleApiService(@GoogleRetrofit retrofit: Retrofit): GoogleApiService = retrofit.create(GoogleApiService::class.java)

    /**
     * Kakao Api
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KakaoOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KakaoRetrofit

    @Provides
    @Singleton
    @KakaoOkHttpClient
    fun provideKakaoOkHttpClient(cache: Cache, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient{
        return OkHttpClient.Builder().apply {
            cache(cache)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
                    .build()
                it.proceed(request)
            }
            addInterceptor(httpLoggingInterceptor)
        }.build()

    }

    @Provides
    @Singleton
    @KakaoRetrofit
    fun provideKakaoRetrofit(@KakaoOkHttpClient client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideKakaoApiService(@KakaoRetrofit retrofit: Retrofit): KakaoApiService = retrofit.create(KakaoApiService::class.java)

    /**
     * KakaoMobility Api
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KakaoMobilityOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class KakaoMobilityRetrofit

    @Provides
    @Singleton
    @KakaoMobilityOkHttpClient
    fun provideKakaoMobilityOkHttpClient(cache: Cache, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient{
        return OkHttpClient.Builder().apply {
            cache(cache)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")//Kakao OpenAPI와 Base URL은 다르나, REST Key는 같이 사용
                    .build()
                it.proceed(request)
            }
            addInterceptor(httpLoggingInterceptor)
        }.build()
    }

    @Provides
    @Singleton
    @KakaoMobilityRetrofit
    fun provideKakaoMobilityRetrofit(@KakaoMobilityOkHttpClient client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://apis-navi.kakaomobility.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideKakaoMobilityApiService(@KakaoMobilityRetrofit retrofit: Retrofit): KakaoMobilityApiService = retrofit.create(KakaoMobilityApiService::class.java)

    /**
     * Naver Api
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NaverOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class NaverRetrofit

    @Provides
    @Singleton
    @NaverOkHttpClient
    fun provideNaverOkHttpClient(cache: Cache, httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient{
        return OkHttpClient.Builder().apply {
            cache(cache)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
                    .build()
                it.proceed(request)
            }
            addInterceptor(httpLoggingInterceptor)
        }.build()
    }

    @Provides
    @Singleton
    @NaverRetrofit
    fun provideNaverRetrofit(@NaverOkHttpClient client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://openapi.naver.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverApiService(@NaverRetrofit retrofit: Retrofit): NaverApiService = retrofit.create(NaverApiService::class.java)
}