package com.amazing.stamp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.PostAddModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
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
    private val feedModels: ArrayList<PostAddModel>
) :
    RecyclerView.Adapter<FeedAdapter.Holder>() {

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
            tvItemFeedFootCount.text = "1"

            CoroutineScope(Dispatchers.Main).launch {
                val userModel = getUserModel(model.writer)
                tvItemFeedNickname.text = userModel.nickname

                if (userModel.imageName != null && userModel.imageName != "") {
                    storage.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel.imageName}").downloadUrl.addOnSuccessListener {
                        Glide.with(context).load(it).into(ivItemFeedProfile)
                    }
                }
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
    }
}