package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedImageSlideBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FeedImageViewPagerAdapter(
    private val context: Context,
    private val postId: String,
    private val sliderImage: ArrayList<String>,
) :
    RecyclerView.Adapter<FeedImageViewPagerAdapter.MyViewHolder>() {

    private val storage by lazy { Firebase.storage }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_image_slide, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.run {
            val path = storage.getReference(FirebaseConstants.STORAGE_POST).child(postId)
                .child(sliderImage[position])

            try {
                path.downloadUrl.addOnSuccessListener {
                    Glide.with(context).load(it).into(ivItemSlider)
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun getItemCount(): Int {
        return sliderImage.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedImageSlideBinding.bind(itemView)
    }
}
