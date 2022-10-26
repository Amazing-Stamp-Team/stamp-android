package com.amazing.stamp.pages.sns

import android.os.Bundle
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

class FriendsSearchActivity : ParentActivity() {
    private val TAG = "FriendsSearchActivity"
    private val binding by lazy { ActivityFriendsSearchBinding.inflate(layoutInflater) }
    private val friendsList = ArrayList<UserModel>()
    private var fireStore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private lateinit var friendAdapter: FriendAddAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var userModel: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        fireStore = Firebase.firestore
        storage = Firebase.storage

        setSupportActionBar(binding.toolbarFriendsAdd)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
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

    private fun setUpItemClickEvent() {
        friendAdapter.itemClickListener = object : FriendAddAdapter.ItemClickListener {
            override fun onItemClick(followingUserModel: UserModel) {
                if (userModel.followers == null) {
                    userModel.followers = ArrayList()
                }
                if (userModel.followings == null) {
                    userModel.followings = ArrayList()
                }

                userModel.followings!!.add(followingUserModel.uid)

                showProgress(this@FriendsSearchActivity, "잠시만 기다려주세요")

                fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.document(userModel.uid)
                    ?.set(userModel)
                    ?.addOnCompleteListener {
                        hideProgress()
                        if (it.isSuccessful) {
                            showShortToast(applicationContext, "${followingUserModel.nickname}님을 팔로우합니다")

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

    private fun setTargetUserFollower(targetUid:String) {
        val targetFriendModel = fireStore?.collection(FirebaseConstants.COLLECTION_FRIENDS)?.document(targetUid)?.get()?.addOnSuccessListener {
            val friendModel = it.toObject<FriendModel>()

            if(friendModel?.followers == null) friendModel?.followers = ArrayList()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 앱 바 클릭 이벤트
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}