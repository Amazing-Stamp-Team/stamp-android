package com.amazing.stamp.pages.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stamp.R
import com.example.stamp.databinding.ActivityLocationBasedViewBinding
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class LocationBasedViewActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy { ActivityLocationBasedViewBinding.inflate(layoutInflater) }
    private lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mapFragment = supportFragmentManager.findFragmentById(R.id.fg_map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.fg_map, it).commit()
            }

        mapFragment.getMapAsync(this)

    }


    override fun onMapReady(naverMap: NaverMap) {
        naverMap.uiSettings.isLocationButtonEnabled = true

        val marker1 = Marker()
        marker1.position = com.naver.maps.geometry.LatLng(37.5697102, 126.924881)
        marker1.map = naverMap


        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                return "테스트 마커 말풍선"
            }
        }


        locationOverlay.setOnClickListener {
            Toast.makeText(this, "현재 위치", Toast.LENGTH_SHORT).show()
            true
        }


        val image = OverlayImage.fromResource(R.drawable.ic_image)
        marker1.icon = image



        infoWindow.open(marker1)


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