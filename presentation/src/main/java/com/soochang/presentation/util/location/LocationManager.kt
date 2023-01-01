package com.soochang.presentation.util.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task


class LocationManager private constructor(
    private var requiredPermission: String,
    private var locationRequestPriority: Int,

    //위치정보 획득 제한시간
    //첫 위치정보 수신이 오래걸리는경우가 있어 제한된 시간동안(1500) 기다림
    //제한시간동안 위치수신이 안되면 lastKnownLocation사용하고 위치얻기 종료
    private var locationMaxWaitTime: Long
) {

    companion object {
        val TAG: String = this.javaClass.simpleName

        @Volatile
        private lateinit var instance: LocationManager

        @JvmStatic
        fun getInstance(requiredPermission: String, locationRequestPriority: Int, locationMaxWaitTime: Long = 1500L): LocationManager {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = LocationManager(requiredPermission, locationRequestPriority, locationMaxWaitTime)
                }else{
                    instance.requiredPermission = requiredPermission
                    instance.locationRequestPriority = locationRequestPriority
                    instance.locationMaxWaitTime = locationMaxWaitTime
                }
                return instance
            }
        }
    }

    interface LocationFeatureStatusListener{
        fun onLocationFeatureTunedOn()
        fun onLocationFeatureTunedOff(e: Exception)
    }

    interface LocationResultListener{
        fun onLocationReceived(location: Location)
        fun needLocationPermission()
        fun onLocationReceiveFail(exception: Exception?)
    }

    //getCurrentLocation() 사용중 앱종료나 제한시간 경과시 취소시키는데 사용
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val handler = Handler(Looper.getMainLooper())

    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * 위치 얻어오기 진입점
     *
     * 위치기능 켜있는지 확인.
     * 켜저있는경우 위치정보 얻기 함수 실행(getLocation)
     * 꺼져있는경우 위치기능 켜기 시스템 다이얼로그 표시
     * */
    fun startGettingLocation(activity: FragmentActivity, locationFeatureStatusListener: LocationFeatureStatusListener) {
        Log.d(TAG, "startGettingLocation")

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.create()
                .setPriority(locationRequestPriority)
                .setInterval(500)
                .setFastestInterval(100)
                .setMaxWaitTime(locationMaxWaitTime))

        val task = LocationServices.getSettingsClient(activity)
            .checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            Log.d(TAG, "startGettingLocation: 위치설정기능 켜짐상태")
            val states = response.locationSettingsStates
            if ( states !=null && states.isLocationPresent ) {
                locationFeatureStatusListener.onLocationFeatureTunedOn()
            }
        }
        task.addOnFailureListener { e ->
            Log.d(TAG, "startGettingLocation: 위치설정기능 꺼짐상태")

            locationFeatureStatusListener.onLocationFeatureTunedOff(e)
        }
    }

    private fun getFusedLocation(context: Context): FusedLocationProviderClient {
        return fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * 위치얻기
     *
     * 위치권한 승인상태인경우 위치 얻어오기
     * 거부상태인경우 위치 권한요청
     */
    fun getLocation(activity: Context, currentLocationResultListener: LocationResultListener) {
        Log.d(TAG, "getLocation function start")

        val allPermissionGranted = arrayOf(requiredPermission).all { it
            ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }

        if ( allPermissionGranted ) {
            Log.d(TAG, "getLocation: 권한 승인상태")

            cancellationTokenSource ?: CancellationTokenSource()

            //획득된 위치 - current location얻기 실패시, last location사용(둘다 실패시 null)
            var lastLocation: Location? = null

            //위치정보(currentLocation)를 못얻어와 무한히 기다리지 않도록 제한시간 후 종료
            val callbackLocationMaxWaitTimeOver = Runnable {
                if( lastLocation != null ){
                    Log.d(TAG, "getLocation: locationWaitTime(1500ms) 시간동안 기다려도 현재위치 못얻어와 lastLocation사용")

                    //위치수집 중지
                    cancellationTokenSource?.cancel()

                    //위치정보 리턴
                    currentLocationResultListener.onLocationReceived(lastLocation!!)
                }
            }
            handler.postDelayed(callbackLocationMaxWaitTimeOver, locationMaxWaitTime)

            //lastLocation 얻기
            getFusedLocation(activity).lastLocation.addOnSuccessListener { location : Location? ->
                if (location != null) {
                    Log.d(TAG, "getLocation: last Location = $location")

                    //지도에 현재위치 표시 & 지도중심 이동
                    lastLocation = location
                }else{
                    Log.d(TAG, "getLocation: last Location = null")
                }
            }

            //currentLocation 얻기
            val currentLocationTask: Task<Location> = getFusedLocation(activity).getCurrentLocation(
                locationRequestPriority,
                cancellationTokenSource?.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val location: Location = task.result
                    Log.d(TAG, "getLocation: current Location = $location")

                    //위치수집 중지
                    cancellationTokenSource?.cancel()
                    handler.removeCallbacks(callbackLocationMaxWaitTimeOver)

                    //위치정보 리턴
                    currentLocationResultListener.onLocationReceived(location)
                } else {
                    Log.d(TAG, "getLocation: get current Location failure ${task.exception}")

                    currentLocationResultListener.onLocationReceiveFail(task.exception)
                }
            }
        } else {
            Log.d(TAG, "getLocation: 승인된 권한 없음")

            currentLocationResultListener.needLocationPermission()
        }
    }

    fun cancelGettingLocation(){
        // Cancels location request (if in flight).
        cancellationTokenSource?.cancel()
    }
}