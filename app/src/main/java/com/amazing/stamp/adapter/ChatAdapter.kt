package com.amazing.stamp.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.ChatModel
import com.amazing.stamp.models.ChatRoomModel
import com.amazing.stamp.models.UserModel
import com.example.stamp.R
import com.example.stamp.databinding.ItemChatBinding
import com.example.stamp.databinding.ItemChatRoomBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ChatAdapter(
    val context: Context,
    private val chatModels: ArrayList<ChatModel>,
    private val userModels: ArrayList<UserModel>
) :
    RecyclerView.Adapter<ChatAdapter.Holder>() {

    private val auth by lazy { Firebase.auth }
    private val storage by lazy { Firebase.storage }
    private val fireStore by lazy { Firebase.firestore }
    private val TAG = "ChatAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_chat, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = chatModels[position]
            tvItemChatMessage.text = model.content

            tvItemChatName.text = userModels.find { it.uid == model.user }?.nickname

            if (auth.currentUser?.uid == model.user) {
                tvItemChatMessage.setBackgroundResource(R.drawable.ic_talk_me)
                (tvItemChatMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.END

                // 내가 보낸 채팅은 닉네임 숨김
                tvItemChatName.visibility = View.GONE
                tvItemChatMessage.setTextColor(context.getColor(R.color.white))
            } else {
                tvItemChatName.visibility = View.VISIBLE
                tvItemChatMessage.setBackgroundResource(R.drawable.ic_talk_other)
                tvItemChatMessage.gravity = Gravity.START
                tvItemChatMessage.setTextColor(context.getColor(R.color.black))
                (tvItemChatMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            }
        }
    }


    override fun getItemCount(): Int {
        return chatModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatBinding.bind(itemView)
    }
}