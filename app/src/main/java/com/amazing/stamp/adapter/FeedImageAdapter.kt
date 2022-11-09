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
import com.example.stamp.databinding.ItemFeedBinding
import com.example.stamp.databinding.ItemFeedImageBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FeedImageAdapter(
    val context: Context,
    private val uid: String,
    private val imageUri: ArrayList<String>?
) :
    RecyclerView.Adapter<FeedImageAdapter.Holder>() {

    private val storage by lazy { Firebase.storage }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_feed_image, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
//        holder.binding.run {
//            storage.getReference("${FirebaseConstants.STORAGE_PROFILE}/IMG_PROFILE_1kaofPc9DReZJJ1TqqR0wJr2IMg2_1667401347534.png").downloadUrl.addOnSuccessListener {
//                Glide.with(context).load(it).into(ivItemFeedImage)
//            }
//        }

        holder.binding.run {
            storage.getReference("${FirebaseConstants.STORAGE_POST}/$uid/${imageUri!![position]}").downloadUrl.addOnSuccessListener {
                Glide.with(context).load(it).into(ivItemFeedImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return imageUri!!.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedImageBinding.bind(itemView)
    }
}