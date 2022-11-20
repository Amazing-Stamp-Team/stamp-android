package com.amazing.stamp.pages.sns

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.R
import com.example.stamp.databinding.ActivityFollowingListPageBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowingListActivity : ParentActivity(){
    private val binding by lazy { ActivityFollowingListPageBinding.inflate(layoutInflater) }

    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    protected lateinit var friendAdapter: FriendAddAdapter
    protected val friendsList = ArrayList<String>()
    private val friendUserModels = ArrayList<UserModel>()

    private val TAG = "TAG_FOLLOWINGLIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMyFollowingList)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        CoroutineScope(Dispatchers.Main).launch {
            // 1. 내 팔로우/팔로잉 리스트 가져오기 (uid)
            getMyFollowings()
            // 2. 팔로잉 UID 를 기준으로 해당 유저의 UserModel 가져오기
            getMyFollowingsUserModels()
            // 3. 팔로잉을 리사이클러뷰에 적용
            setUpFriendRecyclerView()
        }

    }

    private suspend fun getMyFollowings() {
        // Firebase 프렌지 콜렉션을 가져옴
        val docRef = fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS).document(auth.uid!!).get().await()
        docRef.toObject<FriendModel>()?.followings?.forEach { friendUid ->
            friendsList.add(friendUid)
        }
    } //파이어스토어의 Friends 컬렉션에서 팔로잉하는 친구들(uid)을 불러온 후, 팔로잉 목록을 반복문으로 문자열 배열에 집어넣는다

    private suspend fun getMyFollowingsUserModels() {
        friendsList.forEach {  friendUid ->
            val model = fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(friendUid).get().await()
            friendUserModels.add(model.toObject<UserModel>()!!)
        }
    } // 팔로잉 중인 친구들을 모두 User 모델에 순서대로 전부 User모델을 형식으로 add

    private fun setUpFriendRecyclerView() {
        friendAdapter = FriendAddAdapter(applicationContext, friendUserModels)
        binding.rvMyFollowingList.adapter = friendAdapter
        friendAdapter.notifyDataSetChanged()

        friendAdapter.itemClickListener = object : FriendAddAdapter.ItemClickListener {
            override fun onItemClick(userModel: UserModel) {
                removeMyFollowing(userModel.uid)
                removeMeTheirFollower(userModel.uid)
                Toast.makeText(applicationContext, "${userModel.nickname}님을 언팔로우 했습니다", Toast.LENGTH_SHORT).show()

            }
        }
    }
    // 내 팔로잉 리스트의 해당 유저 삭제
    private fun removeMyFollowing(targetUid: String) {
        // ArrayUnion -> 배열에 요소 추가, ArrayRemove -> 배열 요소 삭제

        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS) // 친구 컬렉션에서
            .document(auth.uid!!) // 나의 문서를 찾아
            .update(
                FirebaseConstants.FRIENDS_FIELD_FOLLOWINGS,
                FieldValue.arrayRemove(targetUid),
            ) // 팔로잉에서 상대방 삭제
        

    }

    // 해당 유저 팔로워에 나 삭제
    private fun removeMeTheirFollower(targetUid: String) {
        // ArrayUnion -> 배열에 요소 추가, ArrayRemove -> 배열 요소 삭제

        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS) // 친구 콜렉션에서
            .document(targetUid) // 상대방의 친구 문서를 찾아서
            .update(
                FirebaseConstants.FRIENDS_FIELD_FOLLOWERS,
                FieldValue.arrayRemove(auth.uid)
            ) // 나 삭제
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