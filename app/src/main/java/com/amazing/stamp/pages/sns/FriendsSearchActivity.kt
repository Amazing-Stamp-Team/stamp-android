package com.amazing.stamp.pages.sns

import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.example.stamp.R
import com.example.stamp.databinding.ActivityFriendsSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

open class FriendsSearchActivity : ParentActivity() {
    protected val TAG = "FriendsSearchActivity"
    protected val binding by lazy { ActivityFriendsSearchBinding.inflate(layoutInflater) }
    protected val friendsList = ArrayList<UserModel>()
    protected val fireStore by lazy { Firebase.firestore }
    protected var storage: FirebaseStorage? = null
    protected lateinit var friendAdapter: FriendAddAdapter
    protected lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        storage = Firebase.storage

        binding.ibFriendSearchBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        setUpFriendRecyclerView()
        setUpSearchOption()
        setUpItemClickEvent()
    }


    private fun setUpFriendRecyclerView() {
        friendAdapter = FriendAddAdapter(applicationContext, storage, fireStore, friendsList)
        binding.rvFriends.adapter = friendAdapter
        friendAdapter.notifyDataSetChanged()
    }

    private fun setUpSearchOption() {
        binding.etFriendsSearch.addTextChangedListener {
            friendAdapter.fireStoreSearch(it.toString())
        }
    }

    protected open fun setUpItemClickEvent() {
        friendAdapter.itemClickListener = object : FriendAddAdapter.ItemClickListener {
            override fun onItemClick(profile: ByteArray?, followingUserModel: UserModel) {

                addMyFollowing(followingUserModel.uid)
                addMeTheirFollower(followingUserModel.uid)

                showShortToast("${followingUserModel.nickname}님을 팔로우합니다")


//                fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(userModel.uid)
//                    ?.set(userModel)
//                    ?.addOnCompleteListener {
//                        hideProgress()
//                        if (it.isSuccessful) {
//                            showShortToast("${followingUserModel.nickname}님을 팔로우합니다")
//
//                            // 내 팔로잉 리스트에 해당 유저 추가
//                            setCurrentUserFollowing()
//                            // 내가 팔로잉 하는 유저의 팔로워에 나 추가
//                            setTargetUserFollower(followingUserModel.uid)
//                        } else {
//                            showShortToast(applicationContext, "팔로우 실패")
//                        }
//                    }
            }
        }
    }

    // 내 팔로잉 리스트에 해당 유저 추가
    private fun addMyFollowing(targetUid :String) {
        // val friendModel = FriendModel(userModel.followers!!, userModel.followings!!)
//        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS)
//            ?.document(auth.uid!!)
//            ?.update(FirebaseConstants.FRIENDS_FIELD_FOLLOWINGS, userModel.followings)


        // ArrayUnion -> 배열에 요소 추가, ArrayRemove -> 배열 요소 삭제
        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS) // 친구 컬렉션에서
            .document(auth.uid!!) // 나의 문서를 찾아
            .update(FirebaseConstants.FRIENDS_FIELD_FOLLOWINGS, FieldValue.arrayUnion(targetUid)) // 팔로잉에 상대방 추가
    }

    // 해당 유저 팔로워에 나 추가
    private fun addMeTheirFollower(targetUid: String) {
//        fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(targetUid)?.get()
//            ?.addOnSuccessListener {
//                val friendModel = it.toObject<FriendModel>() ?: FriendModel(null, null)
//
//                if (friendModel.followers == null) friendModel.followers = ArrayList()
//
//                friendModel.followers?.add(auth.uid!!)
//
//                fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(targetUid)
//                    ?.set(friendModel)
//            }

//        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS).document(targetUid).get()
//            .addOnSuccessListener {
//                var friendModel = it.toObject<FriendModel>()
//                if (friendModel == null) friendModel = FriendModel(null, null)
//
//                var followers = friendModel.followers
//                if (followers == null) followers = ArrayList()
//                followers.add(auth.uid!!)
//
//                fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS).document(targetUid)
//                    .update(FirebaseConstants.FRIENDS_FIELD_FOLLOWERS, followers)
//            }

        fireStore.collection(FirebaseConstants.COLLECTION_FRIENDS) // 친구 콜렉션에서
            .document(targetUid) // 상대방의 친구 문서를 찾아서
            .update(FirebaseConstants.FRIENDS_FIELD_FOLLOWERS, FieldValue.arrayUnion(auth.uid)) // 나 추가

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}