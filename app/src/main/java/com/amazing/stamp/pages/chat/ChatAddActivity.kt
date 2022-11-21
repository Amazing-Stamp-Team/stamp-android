package com.amazing.stamp.pages.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.amazing.stamp.adapter.ProfileNicknameAdapter
import com.amazing.stamp.api.NaverAPI
import com.amazing.stamp.api.dto.NaverMapSearchResponseDTO
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.pages.map.MapSearchActivity
import com.amazing.stamp.pages.sns.FriendsTagActivity
import com.amazing.stamp.pages.sns.PostAddActivity
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityChatAddBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatAddActivity : ParentActivity() {
    private val binding by lazy { ActivityChatAddBinding.inflate(layoutInflater) }
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }
    private val friends = ArrayList<ProfileNicknameModel>()
    private val visitorAdapter by lazy { ProfileNicknameAdapter(applicationContext, friends) }
    private var address: String? = null
    private var addressTitle: String? = null
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarChatAdd)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        binding.run {
            etChatAddLocation.setOnClickListener {
                val intent = Intent(applicationContext, MapSearchActivity::class.java)
                startActivityForResult(intent, Constants.EXTRA_MAP_SEARCH_REQUEST_CODE)
            }

            btnChatVisitorAdd.setOnClickListener {
                // 친구 언급 버튼
                val intent = Intent(applicationContext, FriendsTagActivity::class.java)
                startActivityForResult(intent, PostAddActivity.FRIEND_SEARCH_REQUEST_CODE)
            }

            setUpFriendVisit()

            btnChatAddFinish.setOnClickListener { onChatRoomAdd() }
        }
    }

    private fun setUpFriendVisit() {
        binding.rvVisitors.adapter = visitorAdapter
        visitorAdapter.onItemRemoveClickListener =
            object : ProfileNicknameAdapter.OnItemRemoveClickListener {
                override fun onItemRemoved(model: ProfileNicknameModel, position: Int) {
                    friends.removeIf { it.uid == model.uid }
                    visitorAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun onChatRoomAdd() {
        binding.run {
            val chatRoomName = etChatAddTitle.text.toString()
            val chatRoomIntroduce = etChatAddIntroduce.text.toString()

            val visitors = ArrayList<String>()
            visitors.add(auth.currentUser!!.uid)
            friends.forEach { visitors.add(it.uid) }

            val chatRoomModel =
                ChatRoomModel(
                    chatRoomName,
                    chatRoomIntroduce,
                    addressTitle,
                    address,
                    auth.currentUser!!.uid,
                    visitors
                )

            fireStore.collection(FirebaseConstants.COLLECTION_CHAT)
                .add(chatRoomModel).addOnCompleteListener {
                    if (it.isSuccessful) {
                        finish()
                    } else {
                        showShortToast("채팅방 생성 실패")
                    }
                }
        }
    }

    private fun tagFriend(uid: String, nickname: String) {
        // 태그된 사용자 리스트에 존재하지 않을 경우 (idx == -1) 사용자를 추가
        // 태그된 사용자 리스트에 있으면 이미 태그된 사용자입니다 메시지 출력
        if (friends.indexOfFirst { it.uid == uid } == -1) {
            friends.add(ProfileNicknameModel(uid, nickname))
            visitorAdapter.notifyDataSetChanged()
        } else {
            showShortToast("이미 태그된 사용자입니다")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CANCELED) return

        when (requestCode) {
            // 친구 태그 RequestCode 일때
            PostAddActivity.FRIEND_SEARCH_REQUEST_CODE -> {
                val uid = data?.getStringExtra(PostAddActivity.INTENT_EXTRA_UID)
                val nickname = data?.getStringExtra(PostAddActivity.INTENT_EXTRA_NAME)
                if (uid != null && nickname != null) {
                    tagFriend(uid, nickname)
                }
            }
            Constants.EXTRA_MAP_SEARCH_REQUEST_CODE -> {
                addressTitle = data?.getStringExtra(Constants.INTENT_EXTRA_MAP_TITLE)
                address = data?.getStringExtra(Constants.INTENT_EXTRA_ADDRESS)
                category = data?.getStringExtra(Constants.INTENT_EXTRA_MAP_CATEGORY)

                binding.etChatAddLocation.setText(addressTitle)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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
}