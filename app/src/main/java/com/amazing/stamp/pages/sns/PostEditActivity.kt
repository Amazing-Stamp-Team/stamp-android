package com.amazing.stamp.pages.sns

import android.os.Bundle
import android.util.Log
import android.view.View
import com.amazing.stamp.models.PostLikeModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

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

        binding.btnPostAddFinish.setOnClickListener { onPostEdit() }
    }

    private fun onPostEdit() {
        //    val writer: String,
        //    val friends: ArrayList<String>,
        //    val content: String?,
        //    val startDate: LocalDate?,
        //    val endDate: LocalDate?,
        //    val createdAt: LocalDateTime

        val friendsUID = ArrayList<String>()
        taggedFriends.forEach { friendsUID.add(it.uid) }

        // 구글 파이어베이스의 Timestamp 타입 사용
        val createdAt = Timestamp.now()
        val startTimeStamp = if (binding.etPostDurationStart.text.isEmpty()) null else Timestamp(Date(startDate.timeInMillis))
        val endTimeStamp = if (binding.etPostDurationEnd.text.isEmpty()) null else Timestamp(Date(endDate.timeInMillis))



        showProgress(this, "게시글 수정 중...")

        // 코틀린 코루틴의 Dispatchers 에는 여러 종류가 있음
        // IO - 네트워크 작업 최적화
        // Main - UI와 상호작용
        // Default - CPU를 많이 사용하는 작업
        CoroutineScope(Dispatchers.Main).launch {
            val documentRef = fireStore.collection(FirebaseConstants.COLLECTION_POSTS).document(postId)


            // 내용 수정
            documentRef.update(FirebaseConstants.POSTS_FIELD_CONTENT, binding.etPostWritePost.text.toString())

            // 시작, 끝시간 수정
            documentRef.update(FirebaseConstants.POSTS_FIELD_START_DATE, startTimeStamp)
            documentRef.update(FirebaseConstants.POSTS_FIELD_END_DATE, endTimeStamp)

            // 태그된 친구 수정
            documentRef.update(FirebaseConstants.POSTS_FIELD_FRIENDS, friendsUID)

            // 위치 수정
            documentRef.update(FirebaseConstants.POSTS_FIELD_LOCATION, binding.tvPostLocation.text.toString())




            hideProgress()
            showProgress(this@PostEditActivity, "사진 업로드 중...")


            imageUpload(postId, true)
        }
    }

    private suspend fun getPostData() {
        // 게시물 데이터 가져오기
        postModel =
            fireStore.collection(FirebaseConstants.COLLECTION_POSTS).document(postId).get().await()
                .toObject(PostModel::class.java)!!
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
                fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(friend).get()
                    .addOnSuccessListener { document ->
                        val userModel = document.toObject<UserModel>()
                        if (userModel != null) {
                            taggedFriends.add(
                                ProfileNicknameModel(
                                    userModel.uid,
                                    userModel.nickname
                                )
                            )
                            taggedFriendAdapter.notifyItemInserted(taggedFriends.size - 1)
                        }
                    }
            }
        }


        // 이미지 세팅
        if (postModel.imageNames != null) {
            postModel.imageNames?.forEach { imageName ->
                storage.getReference(FirebaseConstants.STORAGE_POST)
                    .child(postId)
                    .child(imageName).downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "setPostData: $uri")
                        imageUriList.add(uri)

                        refreshImage()
                    }

            }
        }
    }
}