package com.amazing.stamp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            val model = feedModels[position]
            val feedImageAdapter = FeedImageAdapter(context, postIds[position], model.imageNames)
            rvFeedImage.adapter = feedImageAdapter
            feedImageAdapter.notifyDataSetChanged()

            tvItemFeedLocation.text = model.location
            tvItemFeedContent.text = model.content
            tvItemFeedFootCount.text = "0"

            CoroutineScope(Dispatchers.Main).launch {
                val userModel = getUserModel(model.writer)
                tvItemFeedNickname.text = userModel.nickname
                getPostLike(holder.binding, position).toString()

                if (userModel.imageName != null && userModel.imageName != "") {
                    storage.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel.imageName}").downloadUrl.addOnSuccessListener {
                        Glide.with(context).load(it).into(ivItemFeedProfile)
                    }
                }
            }
        }
    }

    private fun getPostLike(binding: ItemFeedBinding, position: Int) {
        val queryResult = fireStore.collection(FirebaseConstants.COLLECTION_POST_LIKES)
            .document(postIds[position]).get().addOnCompleteListener {

                val postLikeModel = it.result.toObject<PostLikeModel>()

                if (auth.currentUser!!.uid in postLikeModel!!.users!!) {
                    isLikeClickeds[position] = true
                    binding.ivItemFeedFoot.imageTintList = ColorStateList.valueOf(Color.RED)
                    binding.tvItemFeedFootCount.text = postLikeModel.users!!.size.toString()
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

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedBinding.bind(itemView)

        init {
            binding.llItemFeedLike.setOnClickListener {
                onLikeClickListener.onLikeClick(binding, postIds[bindingAdapterPosition], bindingAdapterPosition)
            }
        }
    }
}