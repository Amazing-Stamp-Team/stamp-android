package com.amazing.stamp.pages.sns

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.widget.addTextChangedListener
import com.amazing.stamp.adapter.FriendAddAdapter
import com.amazing.stamp.models.FriendModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.ParentActivity
import com.amazing.stamp.utils.Utils.showShortToast
import com.example.stamp.R
import com.example.stamp.databinding.ActivityFriendsSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
    protected var fireStore: FirebaseFirestore? = null
    protected var storage: FirebaseStorage? = null
    protected lateinit var friendAdapter: FriendAddAdapter
    protected lateinit var auth: FirebaseAuth
    protected lateinit var userModel: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        fireStore = Firebase.firestore
        storage = Firebase.storage

        binding.ibFriendSearchBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        CoroutineScope(Dispatchers.IO).launch {
            getUserModel()
        }

        setUpFriendRecyclerView()
        setUpSearchOption()
        setUpItemClickEvent()


    }

    private suspend fun getUserModel() {
        val userModelResult =
            fireStore!!.collection(FirebaseConstants.COLLECTION_USERS).document(auth!!.uid!!).get()
                .await()
        userModel = userModelResult.toObject()!!
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
                if (userModel.followers == null) userModel.followers = ArrayList()
                if (userModel.followings == null) userModel.followings = ArrayList()

                userModel.followings!!.add(followingUserModel.uid)

                showProgress(this@FriendsSearchActivity, "잠시만 기다려주세요")

                fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(userModel.uid)
                    ?.set(userModel)
                    ?.addOnCompleteListener {
                        hideProgress()
                        if (it.isSuccessful) {
                            showShortToast("${followingUserModel.nickname}님을 팔로우합니다")

                            // 내 팔로잉 리스트에 해당 유저 추가
                            setCurrentUserFollowing()
                            // 내가 팔로잉 하는 유저의 팔로워에 나 추가
                            setTargetUserFollower(followingUserModel.uid)
                        } else {
                            showShortToast(applicationContext, "팔로우 실패")
                        }
                    }
            }
        }
    }

    private fun setCurrentUserFollowing() {
        val friendModel = FriendModel(userModel.followers!!, userModel.followings!!)
        fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)
            ?.document(auth.uid!!)
            ?.set(friendModel)
    }

    private fun setTargetUserFollower(targetUid: String) {
        fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(targetUid)?.get()
            ?.addOnSuccessListener {
                val friendModel = it.toObject<FriendModel>() ?: FriendModel(null, null)

                if (friendModel.followers == null) friendModel.followers = ArrayList()

                friendModel.followers?.add(auth.uid!!)

                fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(targetUid)
                    ?.set(friendModel)
            }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}