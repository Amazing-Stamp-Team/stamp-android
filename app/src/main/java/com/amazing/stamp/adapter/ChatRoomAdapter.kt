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
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.models.PostLikeModel
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemChatRoomBinding
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

class ChatRoomAdapter(
    val context: Context,
    private val chatRoomModels: ArrayList<ChatRoomModel>,
) :
    RecyclerView.Adapter<ChatRoomAdapter.Holder>() {

    interface OnChatClickListener {
        fun onChatClick(position: Int)
    }

    var onChatClickListener: OnChatClickListener? = null
    private val auth by lazy { Firebase.auth }
    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val TAG = "FeedAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_chat_room, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            tvItemChatRoomTitle.text = chatRoomModels[position].title
            tvItemChatRoomIntroduce.text = chatRoomModels[position].introduce
            tvItemChatRoomAddressTitle.text = chatRoomModels[position].addressTitle
        }
    }


    override fun getItemCount(): Int {
        return chatRoomModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatRoomBinding.bind(itemView)

        init {
            itemView.setOnClickListener { onChatClickListener?.onChatClick(bindingAdapterPosition) }
        }
    }
}