package com.amazing.stamp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.PostAddModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FeedAdapter(val context: Context, private val postIds:ArrayList<String>, private val feedModels: ArrayList<PostAddModel>) :
    RecyclerView.Adapter<FeedAdapter.Holder>() {

    private val storage by lazy { Firebase.storage }
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
            tvItemFeedNickname.text = model.writer
        }
    }

    override fun getItemCount(): Int {
        return feedModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFeedBinding.bind(itemView)
    }
}