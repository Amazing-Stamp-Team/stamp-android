package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
import com.example.stamp.databinding.ItemFeedImageBinding

class FeedImageAdapter(val context: Context, private val imageUri: ArrayList<String>?) :
    RecyclerView.Adapter<FeedImageAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_feed_image, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            Glide.with(context).load(imageUri!![position]).centerCrop().into(ivItemFeedImage)
        }
    }

    override fun getItemCount(): Int {
        return imageUri!!.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedImageBinding.bind(itemView)
    }
}