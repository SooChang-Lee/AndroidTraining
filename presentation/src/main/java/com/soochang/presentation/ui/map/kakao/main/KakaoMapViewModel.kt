package com.soochang.presentation.ui.map.kakao.main

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.soochang.domain.model.Result
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.domain.model.place.PlaceItems
import com.soochang.domain.repository.openapi.OpenApiRepository
import com.soochang.domain.usecase.place.GetDirectionUseCase
import com.soochang.domain.usecase.place.GetPlaceListByCategoryUseCase
import com.soochang.presentation.ui.base.BaseMVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    private val getPlaceListByCategoryUseCase: GetPlaceListByCategoryUseCase,
    private val getDirectionUseCase: GetDirectionUseCase
): BaseMVIViewModel<KakaoMapContract.Event, KakaoMapContract.State, KakaoMapContract.Effect>() {

    var currentLocation: Location? = null

    //초기 State설정
    override fun createInitialState(): KakaoMapContract.State {
        return KakaoMapContract.State(
            showPlaceCategoryToggleGroup = false,
            showCurrentLocationButton = false,
            showMapTypeToggleGroup = false,

            mapType = KakaoMapContract.MapType.Standard,

            showReloadButton = false,

            loadState = KakaoMapContract.State.LoadState.Idle
        )
    }

    private val _placeItemsResponse = MutableStateFlow(PlaceItems())
    val placeItemsResponse = _placeItemsResponse.asStateFlow()

    private val _directionResponse = MutableStateFlow(Direction())
    val directionResponse = _directionResponse.asStateFlow()
    
    //Event발생(SharedFlow)
    override fun handleEvent(event: KakaoMapContract.Event) {
        when( event ){
            is KakaoMapContract.Event.OnMapViewInitialized ->{
                setState {
                    copy(
                        showPlaceCategoryToggleGroup = true,
                        showCurrentLocationButton = true,
                        showMapTypeToggleGroup = true
                    )
                }
            }

            is KakaoMapContract.Event.OnChangeMapType -> {
                setState {
                    copy(
                        mapType = event.mapType
                    )
                }
            }

            is KakaoMapContract.Event.OnCurrentLocationButtonClicked -> {
                setEffect { KakaoMapContract.Effect.StartGettingLocation }
            }
            is KakaoMapContract.Event.OnReceiveCurrentLocation -> {
                currentLocation = event.location

                setEffect { KakaoMapContract.Effect.CurrentLocationReceived(event.location) }
            }

            is KakaoMapContract.Event.OnMapViewMoved -> TODO()
            is KakaoMapContract.Event.OnRequestPlaceItems -> {
                fetchPlaceItemsByCategory(event.placeCategory, event.rect, event.page)
            }
            is KakaoMapContract.Event.OnReloadButtonClicked -> TODO()

            is KakaoMapContract.Event.OnMapPoiClicked -> {
                setEffect { KakaoMapContract.Effect.MoveToPlaceItem(event.latitude, event.longitude) }
            }

            is KakaoMapContract.Event.OnPlaceItemClicked -> {
                setEffect { KakaoMapContract.Effect.MoveToPlaceItem(event.latitude, event.longitude) }
            }
            is KakaoMapContract.Event.OnPlaceItemDetailClicked -> {
                setEffect { KakaoMapContract.Effect.NavigatePlaceItemDetail(event.placeItem) }
            }
            is KakaoMapContract.Event.OnNavigateRouteClicked -> {
                fetchDirections(event.location, event.placeItem)
            }
        }
    }

    private fun fetchPlaceItemsByCategory(placeCategory: PlaceCategory, rect: String, currentPage: Int) {
        setState {
            copy(
                loadState = KakaoMapContract.State.LoadState.Loading
            )
        }

        viewModelScope.launch {
            val result = getPlaceListByCategoryUseCase(
                OpenApiRepository.PlaceDataSource.KakaoPlace,
                placeCategory,
                rect,
                currentPage,
                COUNT_PER_PAGE
            )

            setState {
                copy(
                    loadState = KakaoMapContract.State.LoadState.Idle
                )
            }

            when( result ){
                is Result.Success -> {
                    _placeItemsResponse.emit(result.data)
                }
                is Result.Error -> {

                }
            }
        }
    }

    private fun fetchDirections(location: Location, placeItem: PlaceItem) {
        setState {
            copy(
                loadState = KakaoMapContract.State.LoadState.Loading
            )
        }

        viewModelScope.launch {
            val result = getDirectionUseCase(
                OpenApiRepository.DirectionDataSource.KakaoMobility,
                "${location.longitude},${location.latitude},name=현재위치",
                "${placeItem.longitude},${placeItem.latitude},name=${placeItem.placeName}"
            )

            setState {
                copy(
                    loadState = KakaoMapContract.State.LoadState.Idle
                )
            }

            when( result ){
                is Result.Success -> {
                    _directionResponse.emit(result.data)
                }
                is Result.Error -> {

                }
            }
        }
    }

    companion object{
        const val COUNT_PER_PAGE: Int = 15
    }
}