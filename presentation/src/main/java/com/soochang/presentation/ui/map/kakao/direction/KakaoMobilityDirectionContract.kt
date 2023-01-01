package com.soochang.presentation.ui.map.kakao.direction

import android.location.Location
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.presentation.ui.base.UiEffect
import com.soochang.presentation.ui.base.UiEvent
import com.soochang.presentation.ui.base.UiState

class KakaoMobilityDirectionContract {
    data class State(
        //맵타입 선택버튼 표시여부
        val showMapTypeToggleGroup: Boolean,

        //맵타입(일반/위성)
        val mapType: MapType,

        //로딩상태
        val loadState: LoadState

    ): UiState{
        enum class LoadState {
            Loading, Idle
        }
    }

    sealed class Event: UiEvent {
        //맵뷰 초기화됨
        object OnMapViewInitialized: Event()

        //지도타입 변경버튼 클릭(스탠다드/위성)
        class OnChangeMapType(val mapType: MapType): Event()

        //지도 초기화 완료 후 길찾기 정보 요청
        class StartGettingDirection(val origin: String, val destination: String): Event()
    }

    sealed class Effect: UiEffect {
        //길찾기 정보 수신됨
        object DirectionReceived : Effect()
    }

    enum class MapType {
        Standard, Hybrid
    }
}