package com.amazing.stamp.pages.sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils
import com.example.stamp.R
import com.example.stamp.databinding.ActivityPostAddBinding
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostEditActivity : PostAddActivity() {
    private lateinit var postId: String
    private lateinit var postModel: PostModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        postId = intent.getStringExtra(Constants.INTENT_EXTRA_POST_ID)!!

        binding.toolbarPostAdd.title = "게시물 수정"

        onDefaultSetting()

        CoroutineScope(Dispatchers.Main).launch {
            getPostData()
            setPostData()
        }
    }

    private suspend fun getPostData() {
        // 게시물 데이터 가져오기
        postModel = fireStore.collection(FirebaseConstants.COLLECTION_POSTS).document(postId).get().await().toObject(PostModel::class.java)!!
    }

    private fun setPostData() {

        // 게시물 데이터 세팅
        binding.etPostWritePost.setText(postModel.content)
        binding.tvPostLocation.text = postModel.location
        binding.tvPostLocation.visibility = View.VISIBLE

        // 날짜 세팅
        if (postModel.startDate != null) {
            startDate.timeInMillis = postModel.startDate!!.seconds * 1000
            binding.etPostDurationStart.setText(Utils.sliderDateFormat.format(startDate.timeInMillis))
        } else {
            binding.etPostDurationStart.setText("")
        }

        if (postModel.endDate != null) {
            endDate.timeInMillis = postModel.endDate!!.seconds * 1000
            binding.etPostDurationEnd.setText(Utils.sliderDateFormat.format(endDate.timeInMillis))
        } else {
            binding.etPostDurationEnd.setText("")
        }


        // 친구 세팅
        if (postModel.friends != null) {
            postModel.friends?.forEach { friend ->
                fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(friend).get().addOnSuccessListener { document ->
                    val userModel = document.toObject<UserModel>()
                    if (userModel != null) {
                        Log.d(TAG, "setPostData: ${userModel.uid} ${userModel.nickname}")
                        taggedFriends.add(ProfileNicknameModel(userModel.uid, userModel.nickname))
                        taggedFriendAdapter.notifyItemInserted(taggedFriends.size - 1)
                    }
                }
            }
        }


        // 이미지 세팅

    }
}