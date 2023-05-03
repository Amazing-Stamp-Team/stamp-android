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
import com.amazing.stamp.api.dto.Item
import com.amazing.stamp.api.dto.NaverMapSearchResponseDTO
import com.amazing.stamp.models.PostLikeModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
import com.example.stamp.databinding.ItemMapSearchBinding
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

class MapSearchAdapter(
    private val context: Context,
    private val mapSearchModels: ArrayList<Item>
) :
    RecyclerView.Adapter<MapSearchAdapter.Holder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    var onItemClickListener: OnItemClickListener? = null
    private val TAG = "FeedAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_map_search, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val models = mapSearchModels[position]

            tvItemMapTitle.text = models.title
            tvItemMapAddress.text = models.address
            tvItemCategory.text = models.category
        }
    }

    override fun getItemCount(): Int {
        return mapSearchModels.size
    }


    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemMapSearchBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(bindingAdapterPosition)
            }
        }
    }
}