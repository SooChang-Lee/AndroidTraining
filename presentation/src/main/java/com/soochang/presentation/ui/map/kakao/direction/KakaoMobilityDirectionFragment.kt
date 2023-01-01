package com.soochang.presentation.ui.map.kakao.direction

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.button.MaterialButtonToggleGroup
import com.soochang.domain.model.direction.Direction
import com.soochang.presentation.R
import com.soochang.presentation.databinding.FragmentKakaoMapDirectionBinding
import com.soochang.presentation.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class KakaoMobilityDirectionFragment : BaseFragment<FragmentKakaoMapDirectionBinding>(FragmentKakaoMapDirectionBinding::inflate),
    View.OnClickListener, MaterialButtonToggleGroup.OnButtonCheckedListener{
    private val viewModel: KakaoMobilityDirectionViewModel by viewModels()

    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupObserver()
    }

    private fun setupUI(){
        //Navigation component 툴바 연동
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        //지도 추가
        mapView = MapView(requireActivity())
        mapView.setMapViewEventListener(mapViewEventListener)

        binding.mapViewContainer.addView(mapView)

        //버튼 리스너 할당
        //맵타입(기본/위성) 토글버튼 체인지 리스너
        binding.toggleGroupMapType.addOnButtonCheckedListener(this)

        //맵타입(기본/위성) 버튼 리스너
        binding.btnMapTypeStandard.setOnClickListener(this)
        binding.btnMapTypeHubrid.setOnClickListener(this)        
    }

    private fun setupObserver(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //UiState 수신
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        Log.d(TAG, "observeViewmodel: uiState ${uiState}")

                        //페이지상태 제어
                        binding.toggleGroupMapType.isVisible = uiState.showMapTypeToggleGroup

                        //MapType
                        if( uiState.mapType == KakaoMobilityDirectionContract.MapType.Standard && mapView.mapType != MapView.MapType.Standard ){
                            mapView.mapType = MapView.MapType.Standard
                        }else if( uiState.mapType == KakaoMobilityDirectionContract.MapType.Hybrid && mapView.mapType != MapView.MapType.Hybrid ){
                            mapView.mapType = MapView.MapType.Hybrid
                        }

                        binding.progress.isVisible = uiState.loadState == KakaoMobilityDirectionContract.State.LoadState.Loading
                    }
                }

                //Effect수신
                launch {
                    viewModel.effect.collect{ uiEffect ->
                        when (uiEffect) {
                            KakaoMobilityDirectionContract.Effect.DirectionReceived -> {

                            }
                        }
                    }
                }

                //Place정보 수신
                launch {
                    viewModel.directionResponse.collectLatest{ direction ->
                        //장소목록에 장소 표시 및 지도에 마커 추가
                        showDirection(direction)
                    }
                }
            }
        }
    }
    //KakaoMap MapViewEventListener
    val mapViewEventListener = object:MapView.MapViewEventListener{
        override fun onMapViewInitialized(p0: MapView?) {
            //맵뷰 초기화이벤트 전달(각종 지도 조작버튼 활성화 Effect 발생시켜주기)
            viewModel.setEvent(
                KakaoMobilityDirectionContract.Event.OnMapViewInitialized
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
        }

        override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        }

        override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
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
                                KakaoMobilityDirectionContract.Event.OnChangeMapType(
                                    KakaoMobilityDirectionContract.MapType.Standard
                                )
                            )
                        }
                        R.id.btn_map_type_hubrid -> {
                            viewModel.setEvent(
                                KakaoMobilityDirectionContract.Event.OnChangeMapType(
                                    KakaoMobilityDirectionContract.MapType.Hybrid
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
        }
    }

    fun showDirection(direction: Direction){

    }
}