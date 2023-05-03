package com.amazing.stamp.pages.sns

import android.content.ClipData.newIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.amazing.stamp.adapter.FeedImageAdapter
import com.amazing.stamp.adapter.FeedImageViewPagerAdapter
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.Constants
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
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

        postId = intent.getStringExtra(Constants.INTENT_EXTRA_POST_ID)


        binding.toolbarPost.run {
            navigationIcon = getDrawable(R.drawable.ic_arrow_back)

            setNavigationOnClickListener { finish() }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_post_delete -> {
                        deletePost()
                        true
                    }

                    R.id.menu_post_edit -> {
                        editPost()
                        true
                    }
                    else -> false
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            val postTask = fireStore.collection(FirebaseConstants.COLLECTION_POSTS)
                .document(postId!!)
                .get()
                .await()


            postModel = postTask.toObject<PostModel>()
            val postId = postTask.id



            if(postModel!!.imageNames != null) {
                binding.includedFeed.vpHome.offscreenPageLimit = 1
                binding.includedFeed.vpHome.adapter = FeedImageViewPagerAdapter(applicationContext, postId, postModel!!.imageNames!!)

                binding.includedFeed.vpHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setCurrentIndicator(position)
                    }
                })

                setupIndicators(0)
            }



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
                .child("${auth.uid!!}.png").downloadUrl.addOnSuccessListener {
                    Glide.with(applicationContext).load(it)
                        .into(binding.includedFeed.ivItemFeedProfile)
                }
        }
    }

    private fun editPost() {
        val intent = Intent(applicationContext, PostEditActivity::class.java)
        intent.putExtra(Constants.INTENT_EXTRA_POST_ID, postId)
        startActivity(intent)
        finish()
    }

    private fun deletePost() {
        fireStore.collection(FirebaseConstants.COLLECTION_POSTS).document(postId!!)
            .delete()
            .addOnSuccessListener {
                finish()
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

    private fun setupIndicators(count: Int) {
        val indicators: Array<ImageView?> = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 8, 16, 8)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.bg_indicator_inactive
                )
            )
            indicators[i]!!.setLayoutParams(params)
            binding.includedFeed.layoutIndicators.addView(indicators[i])
        }
        setCurrentIndicator(0)
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount: Int = binding.includedFeed.layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView: ImageView = binding.includedFeed.layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.bg_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.bg_indicator_inactive
                    )
                )
            }
        }
    }
}