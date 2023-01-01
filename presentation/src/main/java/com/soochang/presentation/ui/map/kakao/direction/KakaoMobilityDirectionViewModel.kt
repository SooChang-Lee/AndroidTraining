package com.soochang.presentation.ui.map.kakao.direction

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.soochang.domain.model.Result
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
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
class KakaoMobilityDirectionViewModel @Inject constructor(
    private val getDirectionUseCase: GetDirectionUseCase
): BaseMVIViewModel<KakaoMobilityDirectionContract.Event, KakaoMobilityDirectionContract.State, KakaoMobilityDirectionContract.Effect>() {

    //초기 State설정
    override fun createInitialState(): KakaoMobilityDirectionContract.State {
        return KakaoMobilityDirectionContract.State(
            showMapTypeToggleGroup = false,

            mapType = KakaoMobilityDirectionContract.MapType.Standard,

            loadState = KakaoMobilityDirectionContract.State.LoadState.Idle
        )
    }

    private val _directionResponse = MutableStateFlow(Direction())
    val directionResponse = _directionResponse.asStateFlow()

    //Event발생(SharedFlow)
    override fun handleEvent(event: KakaoMobilityDirectionContract.Event) {
        when( event ){
            is KakaoMobilityDirectionContract.Event.OnMapViewInitialized ->{
                setState {
                    copy(
                        showMapTypeToggleGroup = true
                    )
                }
            }

            is KakaoMobilityDirectionContract.Event.OnChangeMapType -> {
                setState {
                    copy(
                        mapType = event.mapType
                    )
                }
            }
            is KakaoMobilityDirectionContract.Event.StartGettingDirection -> {
                fetchDirections(event.origin, event.destination)
            }
        }
    }

    private fun fetchDirections(origin: String, destination: String) {
        setState {
            copy(
                loadState = KakaoMobilityDirectionContract.State.LoadState.Loading
            )
        }

        viewModelScope.launch {
            val result = getDirectionUseCase(
                OpenApiRepository.DirectionDataSource.KakaoMobility,
                origin,
                destination
            )

            setState {
                copy(
                    loadState = KakaoMobilityDirectionContract.State.LoadState.Idle
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