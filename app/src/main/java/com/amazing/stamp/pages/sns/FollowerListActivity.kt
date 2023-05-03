package com.amazing.stamp.pages.sns

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.databinding.ActivityFollowerListPageBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowerListActivity : ParentActivity() {
    private val binding by lazy { ActivityFollowerListPageBinding.inflate(layoutInflater) }

    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    protected lateinit var friendAdapter: FriendAddAdapter
    protected val friendsList = ArrayList<String>()
    private var profileArrayList = ArrayList<ByteArray?>()
    private val friendUserModels = ArrayList<UserModel>()
    private val TAG = "TAG_FOLLOWERLIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMyFollowerList)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        CoroutineScope(Dispatchers.Main).launch {
            // 1. 내 팔로우/팔로잉 리스트 가져오기 (uid)
            getMyFollowers()
            // 2. 팔로워 UID 를 기준으로 해당 유저의 UserModel 가져오기
            getMyFollowersUserModels()
            // 3. 팔로워를 리사이클러뷰에 적용
            setUpFriendRecyclerView()
        }
    }


    private suspend fun getMyFollowers() {
        // Firebase 프렌지 콜렉션을 가져옴
        val docRef = fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS).document(auth.uid!!).get().await()
        docRef.toObject<FriendModel>()?.followers?.forEach { friendUid ->
            friendsList.add(friendUid)
        }
    }

    private suspend fun getMyFollowersUserModels() {
        friendsList.forEach {  friendUid ->
            val model = fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(friendUid).get().await()
            friendUserModels.add(model.toObject<UserModel>()!!)
        }
    }

    private fun setUpFriendRecyclerView() {
        friendAdapter = FriendAddAdapter(applicationContext, friendUserModels)
        binding.rvMyFollowerList.adapter = friendAdapter
        friendAdapter.notifyDataSetChanged()

        friendAdapter.itemClickListener = object : FriendAddAdapter.ItemClickListener {
            override fun onItemClick(userModel: UserModel) {
                Toast.makeText(applicationContext, "${userModel.nickname}님을 클릭했습니다", Toast.LENGTH_SHORT).show()
            }
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
}