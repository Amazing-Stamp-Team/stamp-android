package com.amazing.stamp.pages.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amazing.stamp.api.KorTripInfoAPI
import com.amazing.stamp.api.dto.korTripDTO.Item
import com.amazing.stamp.api.dto.korTripDTO.KorTripDTO
import com.amazing.stamp.models.LocationBaseInfoModel
import com.amazing.stamp.utils.SecretConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLocationBasedViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class LocationBasedViewActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1007
    }

    private val binding by lazy { ActivityLocationBasedViewBinding.inflate(layoutInflater) }
    private val markers = ArrayList<Marker>()
    private val korTripInfoAPI by lazy { KorTripInfoAPI.create() }
    private lateinit var fusedLocationSource: FusedLocationSource
    private lateinit var mapFragment: MapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap
    private val TAG = "LocationBasedViewActivity"
    private val korTripDTOs = ArrayList<Item>()
    private val locationBaseInfoModels = ArrayList<LocationBaseInfoModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mapFragment = supportFragmentManager.findFragmentById(R.id.fg_map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.fg_map, it).commit()
            }

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        binding.run {
            ivDesClose.setOnClickListener {
                flDesContainer.visibility = View.GONE
            }

            tvRefresh.setOnClickListener {
                mapSearch()
            }
        }
    }


    private fun mapSearch() {

        // longitude - 경도, x
        // latitude - 위도, y

       val locationOverlay = naverMap.locationOverlay

        val lat = naverMap.cameraPosition.target.latitude
        val lng = naverMap.cameraPosition.target.longitude

        Log.d(TAG, "mapSearch: $lat, $lng")

//        val locationBaseCall = korTripInfoAPI.getLocationBasedTripInfoCall(
//            mapX = lat, mapY = lng, radius = 10000
//        )

        val locationBaseCall = korTripInfoAPI.getLocationBasedTripInfoCall(
            SecretConstants.KOR_TRIP_INFO_SERVICE_KEY,
            lng,
            lat,
            10000,
            "json",
            "AND",
            "STAMF",
            "P",
            300
        )

        markers.forEach { it.map = null }
        markers.clear()
        korTripDTOs.clear()
        locationBaseInfoModels.clear()


        locationBaseCall.enqueue(object : Callback<KorTripDTO> {
            override fun onResponse(call: Call<KorTripDTO>, response: Response<KorTripDTO>) {
                if (response.isSuccessful) {
                    val korTripDTO = response.body()!!
                    val items = korTripDTO.response.body.items.item
                    var idx = 0
                    for (item in items) {
                        Log.d(TAG, "onResponse: idx = ${idx}")
                        if(item.readcount!!.toInt() < 10000) {
                            continue
                        }
                        korTripDTOs.add(item)
                        locationBaseInfoModels.add(LocationBaseInfoModel(item.title!!, item.addr1!!, item.firstimage!!))

                        // longitude - 경도, x
                        // latitude - 위도, y

                        markers.add(Marker().apply {
                            position = LatLng(item.mapy!!.toDouble(), item.mapx!!.toDouble())

                            map = naverMap

                        })
                    }

                    for(i in 0 until markers.size) {
                        markers[i].setOnClickListener {
                            binding.run {
                                tvDesTitle.text = locationBaseInfoModels[i].title
                                tvDesAddress.text = locationBaseInfoModels[i].address

                                try {
                                    Glide.with(applicationContext).load(korTripDTOs[i].firstimage).into(ivDesImage)
                                }catch (e:Exception) {

                                }
                                flDesContainer.visibility = View.VISIBLE
                            }
                            true
                        }
                    }


                }
            }

            override fun onFailure(call: Call<KorTripDTO>, t: Throwable) {
                Toast.makeText(
                    this@LocationBasedViewActivity,
                    "데이터를 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "onFailure: ${t.message}")
                Log.d(TAG, "onFailure: ${t.stackTrace}")
                Log.d(TAG, "onFailure: ${t.cause}")
                Log.d(TAG, "onFailure: ${t.localizedMessage}")
                Log.d(TAG, "onFailure: ${t.suppressed}")
            }
        })
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // naverMap.uiSettings.isLocationButtonEnabled = true


        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                return when (infoWindow.marker?.tag) {
                    1 -> "이순신 장군 동상"
                    2 -> "경복궁"
                    else -> ""
                }
            }
        }

        // 내장 위치 추적 기능 사용
        naverMap.locationSource = fusedLocationSource

        // 네이버맵 현재 가운데에 항상 위치
        val marker = Marker()
        marker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        marker.iconTintColor = Color.parseColor("#293462")
        marker.map = naverMap

        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        // 참고 : https://navermaps.github.io/android-map-sdk/reference/com/naver/maps/map/package-summary.html
        naverMap.addOnCameraChangeListener { reason, animated ->
            marker.position = LatLng(
                // 현재 보이는 네이버맵의 정중앙 가운데로 마커 이동
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 주소 텍스트 세팅 및 확인 버튼 비활성화
            binding.tvCurrentLocation.run {
                text = "위치 이동 중"
                setTextColor(Color.parseColor("#c4c4c4"))
            }
//            binding.btnConfirm.run {
//                setBackgroundResource(R.drawable.rect_round_c4c4c4_radius_8)
//                setTextColor(Color.parseColor("#ffffff"))
//                isEnabled = false
//            }
        }

        // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
        naverMap.addOnCameraIdleListener {
            marker.position = LatLng(
                naverMap.cameraPosition.target.latitude,
                naverMap.cameraPosition.target.longitude
            )
            // 좌표 -> 주소 변환 텍스트 세팅, 버튼 활성화
            binding.tvCurrentLocation.run {
                text = getAddress(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
                setTextColor(Color.parseColor("#2d2d2d"))
            }
//            binding.btnConfirm.run {
//                setBackgroundResource(R.drawable.rect_round_ffd464_radius_8)
//                setTextColor(Color.parseColor("#FF000000"))
//                isEnabled = true
//            }
        }

        locationOverlay.setOnClickListener {
            Toast.makeText(this, "현재 위치", Toast.LENGTH_SHORT).show()
            true
        }

//        marker1.setOnClickListener {
//            Toast.makeText(applicationContext, it.tag.toString(), Toast.LENGTH_SHORT).show()
//            true
//        }
//
//        marker2.setOnClickListener {
//            Toast.makeText(applicationContext, it.tag.toString(), Toast.LENGTH_SHORT).show()
//            true
//        }


//        infoWindow.open(marker1)


        // 사용자 현재 위치 받아오기
        var currentLocation: Location?
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                currentLocation = location
                // 위치 오버레이의 가시성은 기본적으로 false로 지정되어 있습니다. 가시성을 true로 변경하면 지도에 위치 오버레이가 나타납니다.
                // 파랑색 점, 현재 위치 표시
                naverMap.locationOverlay.run {
                    isVisible = true
                    position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                }

                // 카메라 현재위치로 이동
                val cameraUpdate = CameraUpdate.scrollTo(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    )
                )
                naverMap.moveCamera(cameraUpdate)

                // 빨간색 마커 현재위치로 변경
                marker.position = LatLng(
                    naverMap.cameraPosition.target.latitude,
                    naverMap.cameraPosition.target.longitude
                )
            }


    }


    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(applicationContext, Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "주소를 가져 올 수 없습니다."
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // 주소 받아오기
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }


    override fun onStart() {
        super.onStart()
        mapFragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapFragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapFragment.onSaveInstanceState(outState)
    }
}