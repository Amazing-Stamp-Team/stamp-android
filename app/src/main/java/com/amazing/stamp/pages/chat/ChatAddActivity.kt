package com.amazing.stamp.pages.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazing.stamp.adapter.ProfileNicknameAdapter
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.pages.sns.FriendsTagActivity
import com.amazing.stamp.pages.sns.PostAddActivity
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils
import com.example.stamp.databinding.ActivityChatAddBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatAddActivity : ParentActivity() {
    private val binding by lazy { ActivityChatAddBinding.inflate(layoutInflater) }
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }
    private val friends = ArrayList<ProfileNicknameModel>()
    private val visitorAdapter by lazy { ProfileNicknameAdapter(applicationContext, friends) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
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
            val chatRoomLocation = etChatAddLocation.text.toString()

            val visitors = ArrayList<String>()
            visitors.add(auth.currentUser!!.uid)
            friends.forEach { visitors.add(it.uid) }

            val chatRoomModel =
                ChatRoomModel(
                    chatRoomName,
                    chatRoomIntroduce,
                    chatRoomLocation,
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
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}