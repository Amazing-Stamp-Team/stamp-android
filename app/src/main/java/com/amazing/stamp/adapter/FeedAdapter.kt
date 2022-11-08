package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.FeedModel
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding

class FeedAdapter(val context: Context, private val feedModels: ArrayList<FeedModel>) :
    RecyclerView.Adapter<FeedAdapter.Holder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_feed, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = feedModels[position]
            val feedImageAdapter = FeedImageAdapter(context, model.imageUris)

            rvFeedImage.adapter = feedImageAdapter
            feedImageAdapter.notifyDataSetChanged()



            tvItemFeedLocation.text = model.place
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