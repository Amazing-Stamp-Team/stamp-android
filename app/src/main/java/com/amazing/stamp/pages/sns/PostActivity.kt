package com.amazing.stamp.pages.sns

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.amazing.stamp.adapter.FeedImageAdapter
import com.amazing.stamp.adapter.ImageSliderAdapter
import com.amazing.stamp.adapter.PostImageAdapter
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.databinding.ActivityPostBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }
    private var postId: String? = null

    private val fireStore by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    private val storage by lazy { Firebase.storage }

    private val TAG = "TAG_POST"

    private var postModel: PostModel? = null
    private var userModel: UserModel? = null

    private val imageUris = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPost)
        supportActionBar?.run {
            // 앱 바 뒤로가기 버튼 설정
            setDisplayHomeAsUpEnabled(true)
        }

        postId = intent.getStringExtra(Constants.INTENT_EXTRA_POST_ID)


        CoroutineScope(Dispatchers.Main).launch {
            val postTask = fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
                .document(postId!!)
                .get()
                .await()
            postModel = postTask.toObject<PostModel>()
            val postId = postTask.id

            val postImageAdapter = FeedImageAdapter(applicationContext, postId, imageUris)
            binding.includedFeed.rvFeedImage.adapter = postImageAdapter

            postModel?.imageNames?.forEach {
                imageUris.add(it)
            }
            postImageAdapter.notifyDataSetChanged()

            fireStore.collection(FirebaseConstants.COLLECTION_USERS).document(postModel!!.writer)
                .get()
                .addOnSuccessListener {
                    userModel = it.toObject()
                    binding.includedFeed.tvItemFeedNickname.text = userModel?.nickname
                    getUserProfile()
                }

            binding.includedFeed.run {
                tvItemFeedContent.text = postModel?.content
                tvItemFeedLocation.text = postModel?.location
            }
        }
    }

    private fun getUserProfile() {
        if (userModel != null) {
            storage.getReference(FirebaseConstants.STORAGE_PROFILE)
                .child(userModel?.imageName!!).downloadUrl.addOnSuccessListener {
                    Glide.with(applicationContext).load(it)
                        .into(binding.includedFeed.ivItemFeedProfile)
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