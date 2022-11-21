package com.amazing.stamp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.amazing.stamp.models.PostLikeModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FeedAdapter(
    val context: Context,
    private val postIds: ArrayList<String>,
    private val feedModels: ArrayList<PostModel>,
    private val isLikeClickeds: ArrayList<Boolean>
) :
    RecyclerView.Adapter<FeedAdapter.Holder>() {


    interface OnLikeClickListener {
        fun onLikeClick(binding: ItemFeedBinding, postId: String, position: Int)
    }

    lateinit var onLikeClickListener: OnLikeClickListener
    private val auth by lazy { Firebase.auth }
    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val TAG = "FeedAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_feed, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {

            // 뷰를 재사용하기 떄문에 이전에 가져온 프로필 사진이 남아있음
            // 따라서 뷰를 재사용 할 때마다 디폴트 프로파일로 미리 초기화 해줘야함
            Glide.with(context).load(R.drawable.ic_default_profile).into(ivItemFeedProfile)

            val model = feedModels[position]


            tvItemFeedLocation.text = model.location
            tvItemFeedContent.text = model.content
            tvItemFeedFootCount.text = "0"

            // 프로필 사진 가져오기
            CoroutineScope(Dispatchers.Main).launch {
                val userModel = getUserModel(model.writer)
                tvItemFeedNickname.text = userModel.nickname
                getPostLike(holder.binding, position).toString()

                try {
                    storage.getReference(FirebaseConstants.STORAGE_PROFILE)
                        .child("${model.writer}.png").downloadUrl.addOnSuccessListener {
                            Glide.with(context).load(it).into(ivItemFeedProfile)
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if(model.imageNames != null) {
                vpHome.offscreenPageLimit = 1
                vpHome.adapter = FeedImageViewPagerAdapter(context, postIds[position], model.imageNames)

                vpHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setCurrentIndicator(holder.binding, position)
                    }
                })

                setupIndicators(holder.binding, 0)
            }
        }
    }

    private fun setupIndicators(binding: ItemFeedBinding, count: Int) {
        val indicators: Array<ImageView?> = arrayOfNulls<ImageView>(count)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 8, 16, 8)
        for (i in indicators.indices) {
            indicators[i] = ImageView(context)
            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_indicator_inactive
                )
            )
            indicators[i]!!.setLayoutParams(params)
            binding.layoutIndicators.addView(indicators[i])
        }
        setCurrentIndicator(binding, 0)
    }

    private fun getPostLike(binding: ItemFeedBinding, position: Int) {
        val queryResult = fireStore.collection(FirebaseConstants.COLLECTION_POST_LIKES)
            .document(postIds[position]).get().addOnCompleteListener {

                val postLikeModel = it.result.toObject<PostLikeModel>()

                if (auth.currentUser!!.uid in postLikeModel!!.users!!) {
                    isLikeClickeds[position] = true
                    binding.ivItemFeedFoot.imageTintList = ColorStateList.valueOf(Color.RED)
                    binding.tvItemFeedFootCount.text = postLikeModel.users!!.size.toString()
                } else {
                    binding.ivItemFeedFoot.imageTintList = ColorStateList.valueOf(Color.BLACK)
                }
            }
    }

    override fun getItemCount(): Int {
        return feedModels.size
    }

    private suspend fun getUserModel(userUid: String): UserModel {
        return fireStore.collection(FirebaseConstants.COLLECTION_USERS)
            .document(userUid)
            .get().await().toObject()!!
    }

    private fun setCurrentIndicator(binding: ItemFeedBinding, position: Int) {
        val childCount: Int = binding.layoutIndicators.getChildCount()
        for (i in 0 until childCount) {
            val imageView: ImageView = binding.layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_indicator_inactive
                    )
                )
            }
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedBinding.bind(itemView)

        init {
            binding.llItemFeedLike.setOnClickListener {
                onLikeClickListener.onLikeClick(binding, postIds[bindingAdapterPosition], bindingAdapterPosition)
            }
        }
    }
}