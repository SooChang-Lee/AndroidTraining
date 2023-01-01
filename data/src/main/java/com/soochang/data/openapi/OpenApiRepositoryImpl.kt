package com.soochang.data.openapi

import android.util.Log
import com.soochang.data.error.ApiErrorHandler
import com.soochang.data.openapi.google.GoogleApiDataSource
import com.soochang.data.openapi.google.service.GoogleApiService
import com.soochang.data.openapi.kakao.KakaoApiDataSource
import com.soochang.data.openapi.naver.NaverApiDataSource
import com.soochang.domain.model.Result
import com.soochang.domain.model.book.BookItem
import com.soochang.domain.model.book.BookItems
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItems
import com.soochang.domain.repository.openapi.OpenApiRepository
import javax.inject.Inject

class OpenApiRepositoryImpl @Inject constructor(
    private val googleApiDataSource: GoogleApiDataSource,
    private val googleApiService: GoogleApiService,
    private val kakaoApiDataSource: KakaoApiDataSource,
    private val naverApiDataSource: NaverApiDataSource,
    private val apiErrorHandler: ApiErrorHandler
) : OpenApiRepository {
    override suspend fun findBooksByTitle(
        bookDataSource: OpenApiRepository.BookDataSource,
        query: String,
        currentPage: Int,
        countPerPage: Int
    ): Result<BookItems> {
        return try {
            when(bookDataSource){
                OpenApiRepository.BookDataSource.GoogleBooks -> {
                    val volumeListResponse = googleApiDataSource.findVolumesByTitle(query, currentPage, countPerPage)

                    Result.Success(
                        volumeListResponse.toBookItemsModel(currentPage, countPerPage)
                    )
                }
                OpenApiRepository.BookDataSource.KakaoBooks -> {
                    val volumeListResponse = kakaoApiDataSource.findBooksByTitle(query, currentPage, countPerPage)

                    Result.Success(
                        volumeListResponse.toBookItemsModel(currentPage, countPerPage)
                    )
                }
                OpenApiRepository.BookDataSource.NaverBooks -> {
                    val volumeListResponse = naverApiDataSource.findBooksByTitle(query, currentPage, countPerPage)

                    Result.Success(
                        volumeListResponse.toBookItemsModel(currentPage, countPerPage)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "findBookItemsByTitle: Exception $e")

            e.printStackTrace()

            Result.Error(apiErrorHandler.getErrorEntity(e))
        }
    }

//    override suspend fun findBooksByTitlePagingSource(
//        bookDataSource: OpenApiRepository.BookDataSource,
//        query: String,
//        countPerPage: Int
//    ): Flow<PagingData<BookItem>> {
//        return Pager(
//            PagingConfig(pageSize = countPerPage)
//        ) {
//            GoogleBooksPagingSource(googleApiService, apiErrorHandler, query, countPerPage)
//        }.flow
////            .cachedIn(viewModelScope)
//    }

    override suspend fun findBookDetail(bookDataSource: OpenApiRepository.BookDataSource, id: String): Result<BookItem> {
        return try {
            when(bookDataSource) {
                OpenApiRepository.BookDataSource.GoogleBooks -> {
                    val volumeResponse = googleApiDataSource.findVolumeById(id)

                    Result.Success(
                        volumeResponse.toBookItemModel()
                    )
                }
                OpenApiRepository.BookDataSource.KakaoBooks -> {
                    val volumeResponse = kakaoApiDataSource.findBookByIsbn(id)

                    Result.Success(
                        volumeResponse.toBookItemModel()
                    )
                }
                OpenApiRepository.BookDataSource.NaverBooks -> {
                    val volumeResponse = naverApiDataSource.findBookByIsbn(id)

                    Result.Success(
                        volumeResponse.toBookItemModel()
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "findBookItemDetail: Exception $e")

            Result.Error(apiErrorHandler.getErrorEntity(e))
        }
    }

    override suspend fun findPlaceByCategory(
        placeDataSource: OpenApiRepository.PlaceDataSource,
        placeCategory: PlaceCategory,
        rect: String,
        currentPage: Int,
        countPerPage: Int
    ): Result<PlaceItems> {
        return try {
            when(placeDataSource){
                OpenApiRepository.PlaceDataSource.KakaoPlace -> {
                    val volumeListResponse = kakaoApiDataSource.findPlaceByCategory(placeCategory, currentPage, countPerPage, rect)

                    Result.Success(
                        volumeListResponse.toPlaceItemsModel(currentPage, countPerPage)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "findPlaceByCategory: Exception $e")

            e.printStackTrace()

            Result.Error(apiErrorHandler.getErrorEntity(e))
        }
    }

    override suspend fun findDirection(directionDataSource: OpenApiRepository.DirectionDataSource, origin: String, destination: String): Result<Direction> {
        return try {
            when(directionDataSource){
                OpenApiRepository.DirectionDataSource.KakaoMobility -> {
                    val direction = kakaoApiDataSource.findDirection(origin, destination)
                    Result.Success(direction)
                }
                OpenApiRepository.DirectionDataSource.Naver -> TODO()
                OpenApiRepository.DirectionDataSource.Tmap -> TODO()
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "findPlaceByCategory: Exception $e")

            e.printStackTrace()

            Result.Error(apiErrorHandler.getErrorEntity(e))
        }
    }
}