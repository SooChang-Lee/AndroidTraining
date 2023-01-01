package com.soochang.presentation.ui.map.kakao.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButtonToggleGroup
import com.soochang.domain.model.direction.Direction
import com.soochang.domain.model.place.PlaceCategory
import com.soochang.domain.model.place.PlaceItem
import com.soochang.domain.model.place.PlaceItems
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentKakaoMapBinding
import com.soochang.presentation.ui.base.BaseFragment
import com.soochang.presentation.util.CommonUtil
import com.soochang.presentation.util.Constants
import com.soochang.presentation.util.location.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.daum.mf.map.api.*
import java.util.*

/**
 * 2022.09.26
 * 요구사항(가상)
 * 1. 맵타입(일반지도, 위성지도) 버튼 클릭시 맵타입를 변경할 수 있다. - 완료
  * 2. 현재위치 버튼을 클릭하면, GPS 위치정보를 수신하여 지도 위치를 이동시킨다.
 * - 단말의 위치서비스가 켜져있는지, 꺼저있는지 체크하여, 꺼저있다면 켜줄 수 있는 기능을 제공한다.
 * - 위치정보 사용권한이 없다면, 사용자에게 권한허가를 요구한다(위치정보 사용권한이 허가되지 않는다면, 현재위치 수신기능을 사용 할 수 없다).
  * - GPS 위치정보 수신이 제한시간 내에 이루어지지 않는다면, LastKnownLocation을 사용한다.
 * - LastKnownLocation 위치정보도 얻지 못했다면, 위치정보 획득실패 메세지를 표시한다.
 * - 위치정보가 수신되었다면, 수신받은 위치로 지도 중심점 위치를 이동한다.
 * - BottomSheet가 펼쳐져 있다면, 접는다.
 * 3. 카테고리별 장소검색 기능을 제공한다. - 완료
 * - 병원, 약국, 주유소를 선택 할 수 있는 토글버튼을 제공한다. - 완료
 * - 토글버튼 선택시 BottomSheet를 펼쳐서(HALF_EXPAND), 장소목록 검색결과를 리스트에 표시하고, 지도에도 마커로 표시한다. - 완료
 * - 장소목록 페이지가 첫페이지인지, 마지막페이지인지에 따라 이전/다음페이지 버튼을 활성화/비활성화 시켜준다. - 완료
 * - 장소검색 상태에서 지도 위치이동이 일어난경우, 새로고침 버튼을 노출한다.
 * - 새로고침 버튼 클릭시, 현재 지도위치의 장소를 새로 검색하여 장소목록 리스트와 지도의 마커를 갱신해준다.
 * 4. 장소 상세정보 조회 및 길찾기 기능 제공
 * - 장소목록에서 장소를 선택하거나, 지도의 마커를 선택하면 지도 중심점을 장소 위치로 이동한다. - 완료
 * - 장소리스트에 상세정보 버튼, 길찾기 버튼을 보여준다. - 완료
 * - 상세정보 버튼을 누르면 브라우져로 장소 상세검색 페이지를 보유준다. - 완료
 * - 길찾기 버튼을 누르면 새로운 Fragment를 뛰워서 길찾기 정보를 보여준다.
 * 5. BottomSheet 펼침/접힘 상태에 따른 동작 - 완료
 * - BottomSheet가 펼쳐질때 가려지는 지도영역으로 발생하는 불편함을 해소하기 위해, BottomSheet위치에 맞게 지도의 중심점을 자연스럽게 움직여 준다. - 완료
 */

@AndroidEntryPoint
class KakaoMapFragment : BaseFragment<FragmentKakaoMapBinding>(FragmentKakaoMapBinding::inflate),
    KakaoMapPlaceListAdapter.PlaceItemListener, View.OnClickListener, MaterialButtonToggleGroup.OnButtonCheckedListener {
    private val viewModel: KakaoMapViewModel by viewModels()

    private lateinit var mapView: MapView
    private val MarkerTagCurrentLocation = 1
    private val DefaultZoomLevel = 4

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private val initialPeekHeightDP = 40
    private var LastBottomSheetTop = 0

    lateinit var adapter: KakaoMapPlaceListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()
    }

    private fun setupUI(){
        //Navigation component 툴바 연동
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.subtitle = "장소검색, GPS현재위치, 길찾기"

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //BottomSheet를 조작하기 위한 BottomSheetBehavior 초기화
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

        //지도 추가
        mapView = MapView(requireActivity())
        mapView.setOpenAPIKeyAuthenticationResultListener(openAPIKeyAuthenticationResultListener)
        mapView.setMapViewEventListener(mapViewEventListener)
        mapView.setPOIItemEventListener(poiItemEventListener)

        binding.mapViewContainer.addView(mapView)

        //버튼 리스너 할당
        //맵타입(기본/위성) 토글버튼 체인지 리스너
        binding.toggleGroupMapType.addOnButtonCheckedListener(this)

        //맵타입(기본/위성) 버튼 리스너
        binding.btnMapTypeStandard.setOnClickListener(this)
        binding.btnMapTypeHubrid.setOnClickListener(this)

        //현재위치 버튼 리스너
        binding.btnCurrentLocation.setOnClickListener(this)

        //Place검색 토글 버튼
        binding.btnPlaceCategoryHospital.setOnClickListener(this)
        binding.btnPlaceCategoryPharmacy.setOnClickListener(this)
        binding.btnPlaceCategoryGasstation.setOnClickListener(this)

        //Place리스트 이전/다음 페이지 버튼
        binding.btnNextPage.setOnClickListener(this)
        binding.btnPrevPage.setOnClickListener(this)

        mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d(TAG, "onStateChanged: bottomSheetBehaviorState= $newState")

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(TAG, "onSlide: " +
                        "slideOffset=$slideOffset " +
                        "bottomSheetBehavior.peekHeight=${mBottomSheetBehavior.peekHeight} " +
                        "bottomSheetBehavior.expandedOffset=${mBottomSheetBehavior.expandedOffset} " +
                        "mapView.height=${mapView.height} " +
                        "bottomSheet.height=${binding.bottomSheet.height} " +
                        "bottomSheet.top=${binding.bottomSheet.top} "
                )

                //BottomSheet 움직임에 따른 지도 중심점 위치 이동
                val mapCenterPointCoord = mapView.mapCenterPoint
                val mapCenterPointPixel = mapCenterPointCoord.mapPointScreenLocation
                mapCenterPointPixel.y = ((mapView.height / 2) + (LastBottomSheetTop - binding.bottomSheet.top) / 2).toDouble()

                val newMapCenter = MapPoint.mapPointWithScreenLocation(mapCenterPointPixel.x, mapCenterPointPixel.y)
                mapView.setMapCenterPoint(newMapCenter, false)

                LastBottomSheetTop = binding.bottomSheet.top
            }
        })

        //RecyclerView 세팅
        //LayoutManager
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = linearLayoutManager

        //Divider 설정
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.recyclerview_divider_horizontal)!!)
        binding.recyclerView.addItemDecoration(divider)

        //Adapter 설정
        if( !::adapter.isInitialized ){
            adapter = KakaoMapPlaceListAdapter(this)
        }

        //DiffUtil Callback 사용시 setHasStableIds적용하면 데이터 로드시마다 깜빡이는 문제 해소됨
//        if (!adapter.hasObservers()) {
//            adapter.setHasStableIds(true)
//        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //UiState 수신
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        Log.d(TAG, "observeViewmodel: uiState ${uiState}")

                        //페이지상태 제어
                        binding.toggleGroupPlaceCategory.isVisible = uiState.showPlaceCategoryToggleGroup
                        binding.btnCurrentLocation.isVisible = uiState.showCurrentLocationButton
                        binding.toggleGroupMapType.isVisible = uiState.showMapTypeToggleGroup

                        //MapType
                        if( uiState.mapType == KakaoMapContract.MapType.Standard && mapView.mapType != MapView.MapType.Standard ){
                            mapView.mapType = MapView.MapType.Standard
                        }else if( uiState.mapType == KakaoMapContract.MapType.Hybrid && mapView.mapType != MapView.MapType.Hybrid ){
                            mapView.mapType = MapView.MapType.Hybrid
                        }

                        binding.progress.isVisible = uiState.loadState == KakaoMapContract.State.LoadState.Loading
                    }
                }

                //Effect수신
                launch {
                    viewModel.effect.collect{ uiEffect ->
                        when (uiEffect) {
                            KakaoMapContract.Effect.StartGettingLocation -> {
                                startGettingLocation()
                            }
                            is KakaoMapContract.Effect.CurrentLocationReceived -> {
                                receiveCurrentLocation(uiEffect.location, true)
                            }

                            is KakaoMapContract.Effect.MoveToPlaceItem -> {
                                moveMapCenter(uiEffect.latitude, uiEffect.longitude)
                            }
                            is KakaoMapContract.Effect.NavigatePlaceItemDetail -> {
                                val i = Intent(Intent.ACTION_VIEW, Uri.parse(uiEffect.placeItem.placeUrl))
                                startActivity(i)
                            }
                            is KakaoMapContract.Effect.DirectionReceived -> {
                                uiEffect.direction
                            }

                            is KakaoMapContract.Effect.ShowErrorSnackbar -> TODO()
                        }
                    }
                }

                //Place정보 수신
                launch {
                    viewModel.placeItemsResponse.collectLatest{ placeItems ->
                        //장소목록에 장소 표시 및 지도에 마커 추가
                        receivePlaceItems(placeItems)
                    }
                }

                //길찾기정보 수신
                launch {
                    viewModel.directionResponse.collectLatest{ direction ->
                        //장소목록에 장소 표시 및 지도에 마커 추가
                        showDirection(direction)
                    }
                }
            }
        }
    }
    
    val openAPIKeyAuthenticationResultListener = object:MapView.OpenAPIKeyAuthenticationResultListener{
        override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode: Int, resultMessage: String?) {
            Log.d(TAG, "onDaumMapOpenAPIKeyAuthenticationResult: resultCode=$resultCode resultMessage=$resultMessage")
        }
    }
    
    //KakaoMap MapViewEventListener
    val mapViewEventListener = object:MapView.MapViewEventListener{
        override fun onMapViewInitialized(p0: MapView?) {
            //맵뷰 초기화이벤트 전달(각종 지도 조작버튼 활성화 Effect 발생시켜주기)
            viewModel.setEvent(
                KakaoMapContract.Event.OnMapViewInitialized
            )

            viewModel.setEvent(
                KakaoMapContract.Event.OnCurrentLocationButtonClicked
            )
        }

        override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
        }

        override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
        }
    }

    val poiItemEventListener = object: MapView.POIItemEventListener{
        override fun onPOIItemSelected(mMapView: MapView?, mapPOIItem: MapPOIItem?) {
            if (mapPOIItem != null) {
                viewModel.setEvent(
                    KakaoMapContract.Event.OnMapPoiClicked(mapPOIItem.mapPoint.mapPointGeoCoord.latitude, mapPOIItem.mapPoint.mapPointGeoCoord.longitude)
                )
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, mapPOIItem: MapPOIItem?) {
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, mapPOIItem: MapPOIItem?, calloutBalloonButtonType: MapPOIItem.CalloutBalloonButtonType?) {
        }

        override fun onDraggablePOIItemMoved(mapView: MapView?, mapPOIItem: MapPOIItem?, mapPoint: MapPoint?) {
        }
    }

    //토글그룹 선택 리스너
    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        if( group == null ) return

        when( group.id ){
            //맵타입 선택 리스너
            R.id.toggle_group_map_type -> {
                if( isChecked ){
                    when(checkedId){
                        R.id.btn_map_type_standard -> {
                            viewModel.setEvent(
                                KakaoMapContract.Event.OnChangeMapType(
                                    KakaoMapContract.MapType.Standard
                                )
                            )
                        }
                        R.id.btn_map_type_hubrid -> {
                            viewModel.setEvent(
                                KakaoMapContract.Event.OnChangeMapType(
                                    KakaoMapContract.MapType.Hybrid
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    //OnClick리스너
    override fun onClick(v: View?) {
        if( v == null ) return

        when(v.id){
            //장소 카테고리 버튼 클릭
            R.id.btn_place_category_hospital -> {
                searchPlaceItems(PlaceCategory.HOSPITAL, 1)
            }
            R.id.btn_place_category_pharmacy -> {
                searchPlaceItems(PlaceCategory.PHARMACY, 1)
            }
            R.id.btn_place_category_gasstation -> {
                searchPlaceItems(PlaceCategory.GAS_STATION, 1)
            }
            //이전, 다음페이지 클릭
            R.id.btn_next_page,
            R.id.btn_prev_page -> {
                val selectedPlaceCategory = when( binding.toggleGroupPlaceCategory.checkedButtonId ){
                    R.id.btn_place_category_hospital -> PlaceCategory.HOSPITAL
                    R.id.btn_place_category_pharmacy -> PlaceCategory.PHARMACY
                    R.id.btn_place_category_gasstation -> PlaceCategory.GAS_STATION
                    else -> { null }
                }

                val page = when( v.id ){
                    R.id.btn_next_page -> viewModel.placeItemsResponse.value.meta.currentPage + 1
                    R.id.btn_prev_page -> viewModel.placeItemsResponse.value.meta.currentPage - 1
                    else -> { null }
                }

                searchPlaceItems(selectedPlaceCategory!!, page!!)
            }
            //현재위치 버튼
            R.id.btn_current_location -> {
                viewModel.setEvent(KakaoMapContract.Event.OnCurrentLocationButtonClicked)
            }
        }
    }

    //검색요청
    private fun searchPlaceItems(placeCategory: PlaceCategory, page: Int){
        val mapPointBounds = mapView.mapPointBounds
        val rect = "${mapPointBounds.bottomLeft.mapPointGeoCoord.longitude},${mapPointBounds.bottomLeft.mapPointGeoCoord.latitude},${mapPointBounds.topRight.mapPointGeoCoord.longitude},${mapPointBounds.topRight.mapPointGeoCoord.latitude}"

        viewModel.setEvent(KakaoMapContract.Event.OnRequestPlaceItems(placeCategory, rect, page))

        //최초 숨겨진 상태인 BottomSheet 보여주기
        showHiddenBottomSheet()
    }

    //GPS 위치수신 완료시 현재위치 마커 추가
    fun receiveCurrentLocation(location: Location, moveMapCenter: Boolean){
        //현재위치 마커 제거
        //mapView.removePOIItems(), mapView.removePOIItem() 작동 안함
//        val arrCurrentMarker = mapView.poiItems.filter {
//            it.tag == MarkerTagCurrentLocation
//        }.toTypedArray()
//        mapView.removePOIItems(arrCurrentMarker)

        //현재위치 마커 등록
        val mapPoint = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)

        val currentLocationMarker = MapPOIItem()
        currentLocationMarker.tag = MarkerTagCurrentLocation
        currentLocationMarker.itemName = "현재 위치"
        currentLocationMarker.markerType = MapPOIItem.MarkerType.RedPin
        currentLocationMarker.showAnimationType = MapPOIItem.ShowAnimationType.SpringFromGround
        currentLocationMarker.isShowCalloutBalloonOnTouch = false
        currentLocationMarker.mapPoint = mapPoint
        mapView.addPOIItem(currentLocationMarker)

        //현재위치로 이동
        if( moveMapCenter ){
            moveMapCenter(location.latitude, location.longitude)
        }
    }

    //검색결과 수신시 리스트 & 지도에 마커 추가
    private fun receivePlaceItems(placeItems: PlaceItems){
        //RecyclerView 어댑터에 데이터 추가
        adapter.setData(placeItems)

        //이전, 다음페이지 버튼 활성화 상태 제어
        binding.btnPrevPage.isEnabled = placeItems.meta.currentPage > 1
        binding.btnNextPage.isEnabled = !placeItems.meta.isEndPage()

        binding.paginationButtonContainer.visibility = View.VISIBLE

        //------------------------------------------------------------------------------------------
        //지도에 마커 추가
        //현재위치 제외한 모든 마커 지우기
        //mapView.removePOIItems(), mapView.removePOIItem() 작동 안함
//        mapView.poiItems.forEach {
//            mapView.removePOIItems(it)
//        }

        //지정된 마커만 제거하는 기능 작동안하여 모든 마커 제거하고 추가 ㅜㅜ
        mapView.removeAllPOIItems()

        //현재위치 마커 추가(removePOIItems() 버그 해소될때까지 임시로 현재위치 마커 추가해줌)
        if( viewModel.currentLocation != null ){
            receiveCurrentLocation(viewModel.currentLocation!!, false)
        }

        //검색결과 마커 등록
        val arrayMapPOIItem :Array<MapPOIItem> = placeItems.listPlace.map {
            val mapPoint = MapPoint.mapPointWithGeoCoord(it.latitude.toDouble(), it.longitude.toDouble())

            val marker = MapPOIItem()
            marker.tag = it.itemId.toInt()
            marker.itemName = it.placeName
            marker.showAnimationType = MapPOIItem.ShowAnimationType.SpringFromGround
            marker.isShowCalloutBalloonOnTouch = true
            marker.mapPoint = mapPoint
            marker.markerType = when(it.placeCategory.id){
                PlaceCategory.HOSPITAL.id -> MapPOIItem.MarkerType.RedPin
                PlaceCategory.PHARMACY.id -> MapPOIItem.MarkerType.BluePin
                PlaceCategory.GAS_STATION.id -> MapPOIItem.MarkerType.YellowPin
                else -> { MapPOIItem.MarkerType.RedPin }
            }
            mapView.addPOIItem(marker)

            marker
        }.toTypedArray()

        mapView.addPOIItems(arrayMapPOIItem)
    }

    //장소검색시 BottomSheet가 접혀있는경우에 BottomSheet 펼쳐주기
    private fun showHiddenBottomSheet(){
        //최초 BottomSheet 숨김가능한상태에서 숨김 불가능한 상태로 변경
        if( mBottomSheetBehavior.isHideable ){
            mBottomSheetBehavior.peekHeight = CommonUtil.dpToPx(requireContext(), initialPeekHeightDP.toFloat()).toInt()
            mBottomSheetBehavior.isHideable = false

            //BottomSheet 움직인 거리 측정하기 위한 초기값 설정
            LastBottomSheetTop = mapView.height// - mBottomSheetBehavior.peekHeight
        }

        if( mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED ){
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun moveMapCenter(latitude: Double, longitude: Double, animated: Boolean = false){
        val placeCoord = MapPoint.mapPointWithGeoCoord(latitude, longitude)

        val mapCenterPointPixel = placeCoord.mapPointScreenLocation
        mapCenterPointPixel.y = (mapCenterPointPixel.y + ((mapView.height - binding.bottomSheet.top) / 2)).toDouble()

        val newMapCenter = MapPoint.mapPointWithScreenLocation(mapCenterPointPixel.x, mapCenterPointPixel.y)

        mapView.setMapCenterPoint(newMapCenter, true)
    }

    override fun onClick(placeItem: PlaceItem) {
        viewModel.setEvent(
            KakaoMapContract.Event.OnPlaceItemClicked(placeItem.latitude.toDouble(), placeItem.longitude.toDouble())
        )
    }

    //장소목록의 아이템 또는 지도의 마커 선택
    override fun onDetailClick(placeItem: PlaceItem) {
        viewModel.setEvent(
            KakaoMapContract.Event.OnPlaceItemDetailClicked(placeItem)
        )
    }

    override fun onNavigateRouteClick(placeItem: PlaceItem) {
        if( viewModel.currentLocation == null ){
            return
        }

        viewModel.setEvent(
            KakaoMapContract.Event.OnNavigateRouteClicked(viewModel.currentLocation!!, placeItem)
        )
    }

    fun showDirection(direction: Direction){
        mapView.removeAllPolylines()
        mapView.removeAllCircles()

        val route = direction.routes.firstOrNull()
        if( route != null ){
            val section = route.sections.firstOrNull()
            val bound = route.summary.bound

            if( section != null ){
                val roads = section.roads
                val guides = section.guides

                //폴리라인으로 경로 추가하기
                roads.forEachIndexed { index, road ->
                    val vertexes = road.vertexes

                    val arrMapPoint: Array<MapPoint?> = arrayOfNulls(vertexes.size / 2)

                    var tempLongitude: Double = 0.0

                    var mapPointIntext = 0
                    vertexes.forEachIndexed { index, vertexe ->
                        if( (index + 1) % 2 == 0 ){
                            Log.d(TAG, "showDirection: vertexe=$vertexe tempLongitude=$tempLongitude")

                            val mapPoint = MapPoint.mapPointWithGeoCoord(vertexe, tempLongitude)
                            arrMapPoint.set(mapPointIntext, mapPoint)

                            mapPointIntext++
                        }else{
                            tempLongitude = vertexe
                        }
                    }

                    val mapPolyline = MapPolyline()
                    mapPolyline.lineColor = Color.RED
                    mapPolyline.addPoints(arrMapPoint)

                    mapView.addPolyline(mapPolyline)
                }

                //써클로 가이드포인트 추가하기
                guides.forEachIndexed { index, guide ->
                    val mapCircle = MapCircle(
                        MapPoint.mapPointWithGeoCoord(guide.y, guide.x),
                        32,
                        Color.GRAY,
                        Color.YELLOW
                    )

                    mapView.addCircle(mapCircle)
                }
            }

            //Bound 적용하기
            val mapPointBottomLeft = MapPoint.mapPointWithGeoCoord(bound.minY, bound.minX)
            val mapPointTopRight = MapPoint.mapPointWithGeoCoord(bound.maxY, bound.maxX)
            val mapPointBounds = MapPointBounds(mapPointBottomLeft, mapPointTopRight)
            val cameraUpdate = CameraUpdateFactory.newMapPointBounds(mapPointBounds, 80)

            mapView.moveCamera(cameraUpdate)

            mapView.postDelayed({
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }, 1000)
        }
    }

    //----------------------------------------------------------------------------------------------
    //위치권한 획득 & 위치정보 얻기 시작

    //위치권한 요청이 거부된적이 있어 '어플리케이션 설정'페이지로 이동 후 돌아왔을때 권한허용상태 체크하기위한 용도
    private var isLaunchApplicationSettingActivity = false

    private val requiredPermission: String = Manifest.permission.ACCESS_FINE_LOCATION

    private val locationManager = LocationManager.getInstance(
        requiredPermission = requiredPermission,
        locationRequestPriority = Priority.PRIORITY_HIGH_ACCURACY,
        locationMaxWaitTime= 1500L
    )

    /**
     * 위치 얻어오기 진입점
     *
     * 위치기능 켜있는지 확인.
     * 켜저있는경우 위치정보 얻기 함수 실행(locationManager.getLocation)
     * 꺼져있는경우 위치기능 켜기 시스템 다이얼로그 표시
     * */
    private fun startGettingLocation(){
        locationManager.startGettingLocation(requireActivity(), locationFeatureStatusListener)
    }

    //위치기능 꺼진경우 켜기
    //startActivityResult(deprecated) 대신 Activity Result API를 사용
    val locationFeatureResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            Log.d(LocationManager.TAG, "locationFeatureResultLauncher: Activity.RESULT_OK")

            locationManager.getLocation(requireActivity(), locationResultListener)

        }else if (activityResult.resultCode == Activity.RESULT_CANCELED) {
            Log.d(LocationManager.TAG, "locationFeatureResultLauncher: Activity.RESULT_CANCELED")

            startGettingLocation()
        }
    }

    private val locationFeatureStatusListener = object: LocationManager.LocationFeatureStatusListener{
        override fun onLocationFeatureTunedOn() {
            locationManager.getLocation(requireActivity(), locationResultListener)
        }

        override fun onLocationFeatureTunedOff(e: Exception) {
            if (e is ResolvableApiException) {
                try {
                    Log.d(LocationManager.TAG, "startGettingLocation: 위치기능 켜기 시스템 다이얼로그 표시")

                    locationFeatureResultLauncher.launch(IntentSenderRequest.Builder(e.resolution).build())
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "onLocationFeatureTunedOff: $sendEx")
                }
            }
        }
    }

    //권한요청
    //startActivityResult대신 Activity Result API를 사용
    val requestPermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        Log.d(LocationManager.TAG, "activityResultLauncher: permissions.entries = ${permissions.entries}")

        val isGrantAllPermission = permissions.entries.all {
            it.value
        }

        if( isGrantAllPermission ){
            Log.d(LocationManager.TAG, "activityResultLauncher.isGrantAllPermission: true")

            startGettingLocation()
        }else{
            Log.d(LocationManager.TAG, "activityResultLauncher.isGrantAllPermission: false")

            val provideRationale = shouldShowRequestPermissionRationale(requiredPermission)

            if (provideRationale) {
                //사용자가 명시적으로 권한을 거부한경우. 권한이 필요한 이유 설명
                Log.d(LocationManager.TAG, "activityResultLauncher: shouldShowRequestPermissionRationale = true")

                showLocationPermissionRationaleDialog()
            } else {
                //다시묻지않기 선택한 경우
                Log.d(LocationManager.TAG, "activityResultLauncher: shouldShowRequestPermissionRationale = false")

                showApplicationSettingDialog()
            }
        }
    }

    private val locationResultListener = object: LocationManager.LocationResultListener{
        override fun onLocationReceived(location: Location) {
            viewModel.setEvent(KakaoMapContract.Event.OnReceiveCurrentLocation(location))
        }

        override fun needLocationPermission() {
            requestPermissionResultLauncher.launch(arrayOf(requiredPermission))
        }

        override fun onLocationReceiveFail(exception: Exception?) {
            Log.d(TAG, "getLocation: get current Location failure ${exception}")
        }
    }

    //위치권한 획득 & 위치정보 얻기 끝
    //----------------------------------------------------------------------------------------------

    /**
     * 권한이 필요한 이유 설명하는 다이얼로그
     * shouldShowRequestPermissionRationale() true반환된 경우 실행
     */
    private fun showLocationPermissionRationaleDialog(){
        CommonUtil.showDialog(
            requireActivity(),
            "위치정보 승인필요",
            "지도에 현재위치를 표시하기위해 위치권한 승인이 필요합니다\n\n위치정보 권한을 허용해주세요.",
            "권한요청",
            { _, _ ->
                startGettingLocation()
            },
            "다음에",
            { dialogInterface, i ->
                dialogInterface.dismiss()
            },
            cancelable = false
        )
    }

    /**
     * 권한요청이 거부되어 어플리케이션 설정으로 이동
     */
    private fun showApplicationSettingDialog() {
        CommonUtil.showDialog(
            requireActivity(),
            "위치권한 필요",
            "지도에 현재위치를 표시하기위해 위치권한 승인이 필요합니다.\n\n위치정보 권한설정에서 권한을 허용해주세요.",
            "권한설정",
            { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", Constants.APPLICATION_ID, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                isLaunchApplicationSettingActivity = true
            },
            "다음에",
            { dialogInterface, _ ->
                dialogInterface.dismiss()
            },
            cancelable = false
        )
    }
}