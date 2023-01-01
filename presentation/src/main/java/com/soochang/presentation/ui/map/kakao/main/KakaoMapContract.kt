package com.soochang.presentation.ui.map.kakao.main

import android.location.Location
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.presentation.ui.base.UiEffect
import com.soochang.presentation.ui.base.UiEvent
import com.soochang.presentation.ui.base.UiState

class KakaoMapContract {
    data class State(
        //장소유형 토글버튼 표시여부
        val showPlaceCategoryToggleGroup: Boolean,
        //현재위치 버튼 표시여부
        val showCurrentLocationButton: Boolean,
        //맵타입 선택버튼 표시여부
        val showMapTypeToggleGroup: Boolean,

        //맵타입(일반/위성)
        val mapType: MapType,

        //장소 새로고침버튼 표시여부
        val showReloadButton: Boolean,

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

        //맵뷰 이동됨
        class OnMapViewMoved(val coord: String): Event()

        //새로고침 버튼 클릭
        object OnReloadButtonClicked: Event()

        //현재위치 버튼 클릭
        object OnCurrentLocationButtonClicked: Event()
        class OnReceiveCurrentLocation(val location: Location): Event()

        //지도타입 변경버튼 클릭(스탠다드/위성)
        class OnChangeMapType(val mapType: MapType): Event()

        //카테고리버튼 클릭시 1페이지 조회, 이전/다음페이지 버튼 클릭시 해당 페이지 조회
        class OnRequestPlaceItems(val placeCategory: PlaceCategory, val rect: String, val page: Int): Event()

        //장소리스트에서 장소 클릭
        class OnMapPoiClicked(val latitude: Double, val longitude: Double): Event()

        //장소리스트 행 클릭
        class OnPlaceItemClicked(val latitude: Double, val longitude: Double): Event()
        //상세정보 버튼 클릭
        class OnPlaceItemDetailClicked(val placeItem: PlaceItem): Event()
        //길찾기 버튼 클릭
        class OnNavigateRouteClicked(val location: Location, val placeItem: PlaceItem): Event()
    }

    sealed class Effect: UiEffect {
        //현재위치 수집 시작
        object StartGettingLocation : Effect()

        //위치정보 수신시 현재위치로 지도 이동
        class CurrentLocationReceived(val location: Location) : Effect()

        //지정된 위치로 지도 이동(장소 클릭)
        class MoveToPlaceItem(val latitude: Double, val longitude: Double) : Effect()

        //상세정보 화면으로 이동
        class NavigatePlaceItemDetail(val placeItem: PlaceItem) : Effect()

        //길찾기화면으로 이동
        class DirectionReceived(val direction: Direction) : Effect()

        //에러 스낵바 표시
        class ShowErrorSnackbar(val messageId: Int): Effect()
    }

    enum class MapType {
        Standard, Hybrid
    }
}