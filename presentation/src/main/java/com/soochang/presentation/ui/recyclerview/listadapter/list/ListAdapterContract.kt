package com.soochang.presentation.ui.recyclerview.listadapter.list

import android.location.Location
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.presentation.ui.base.UiEffect
import com.soochang.presentation.ui.base.UiEvent
import com.soochang.presentation.ui.base.UiState

class ListAdapterContract {
    data class State(
        val showInitialPage: Boolean = true,
        val showNoDataPage: Boolean = false,
        val showProgress: Boolean = false,

        val loadState: LoadState

    ): UiState{
        enum class LoadState {
            Loading, Idle
        }
    }

    sealed class Event: UiEvent {
        object OnBookItemClicked: Event()
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