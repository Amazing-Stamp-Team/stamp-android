package com.amazing.stamp.pages.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.amazing.stamp.adapter.ChatRoomAdapter
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.databinding.ActivityChatSearchBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatSearchActivity : ParentActivity() {
    private val binding by lazy { ActivityChatSearchBinding.inflate(layoutInflater) }
    private val fireStore by lazy { Firebase.firestore }
    private val chatRoomModels = ArrayList<ChatRoomModel>()
    private val chatRoomAdapter by lazy { ChatRoomAdapter(applicationContext, chatRoomModels) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarChatHome)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }
        currentLocationSet()

        binding.run {
            rvNewChatRooms.adapter = chatRoomAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 앱 바 클릭 이벤트
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun currentLocationSet() {

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            val geocoder = Geocoder(applicationContext)
            val address = geocoder.getFromLocation(it.latitude, it.longitude, 10)

            if (address.size == 0) showShortToast("주소 찾기 오류")
            else {
                // 반환 예시) 대한민국 충청남도 천안시 서북구 두정역동0길 00
                val currentLocation = address[0].getAddressLine(0).replaceFirst("대한민국 ", "")
                val city = currentLocation.split(" ")[1]

                fireStore.collection(FirebaseConstants.COLLECTION_CHAT)


                chatRoomAdapter.notifyDataSetChanged()
            }
        }
    }
}