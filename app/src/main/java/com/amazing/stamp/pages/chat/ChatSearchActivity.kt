package com.amazing.stamp.pages.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazing.stamp.adapter.ChatRoomAdapter
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.databinding.ActivityChatSearchBinding
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ChatSearchActivity : ParentActivity() {
    private val binding by lazy { ActivityChatSearchBinding.inflate(layoutInflater) }
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }
    private val chatRoomModels = ArrayList<ChatRoomModel>()
    private val chatRoomAdapter by lazy { ChatRoomAdapter(applicationContext, chatRoomModels) }
    private val chatIds = ArrayList<String>()

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

            val dividerItemDecoration = DividerItemDecoration(applicationContext, LinearLayoutManager(applicationContext).orientation)
            rvNewChatRooms.addItemDecoration(dividerItemDecoration)
            etChatHomeSearch.addTextChangedListener {
                searchChatRooms(it.toString())
            }

            chatRoomAdapter.onChatClickListener = object : ChatRoomAdapter.OnChatClickListener {
                override fun onChatClick(position: Int) {
                    fireStore.collection(FirebaseConstants.COLLECTION_CHAT).document(chatIds[position])
                        .update(FirebaseConstants.CHAT_FIELD_USERS, FieldValue.arrayUnion(auth.currentUser?.uid))


                    val intent = Intent(this@ChatSearchActivity, ChatActivity::class.java)
                    intent.putExtra(Constants.INTENT_EXTRA_CHAT_ID, chatIds[position])
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun searchChatRooms(keyword:String) {
        if(keyword.isEmpty()) {
            chatRoomModels.clear()
            chatRoomAdapter.notifyDataSetChanged()
            return
        }
        fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
            .get().addOnSuccessListener {
                chatRoomModels.clear()
                chatIds.clear()
                for (document in it) {
                    val chatRoomModel = document.toObject(ChatRoomModel::class.java)
                    if (chatRoomModel.title.contains(keyword) && auth.currentUser!!.uid !in chatRoomModel.users) {
                        chatIds.add(document.id)
                        chatRoomModels.add(chatRoomModel)
                    }
                }
                chatRoomAdapter.notifyDataSetChanged()
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

        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
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
                binding.tvChatHomeLocation.setText(currentLocation)

                val province = currentLocation.split(" ")[0]
                val city = currentLocation.split(" ")[1]

                fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
                    .whereEqualTo(FirebaseConstants.CHAT_FIELD_CITY, city)
                    .get().addOnSuccessListener {
                        chatRoomModels.clear()
                        chatIds.clear()
                        for (document in it) {
                            chatIds.add(document.id)
                            val chatRoomModel = document.toObject(ChatRoomModel::class.java)
                            if(auth.currentUser!!.uid !in chatRoomModel.users) {
                                chatRoomModels.add(chatRoomModel)
                            }
                        }
                        chatRoomAdapter.notifyDataSetChanged()

                        Snackbar.make(binding.root, "현재 위치 근처에서 활동하는 채팅방들을 찾았어요", Snackbar.LENGTH_LONG).show()
                    }
            }
        }
    }
}