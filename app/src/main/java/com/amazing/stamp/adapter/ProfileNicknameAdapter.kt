package com.amazing.stamp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.ProfileNicknameModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemProfileNicknameBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileNicknameAdapter(
    val context: Context,
    val models: ArrayList<ProfileNicknameModel>,
) :
    RecyclerView.Adapter<ProfileNicknameAdapter.Holder>() {

    private val storage by lazy { Firebase.storage }
    interface OnItemRemoveClickListener {
        fun onItemRemoved(model: ProfileNicknameModel, position: Int)
    }

    lateinit var onItemRemoveClickListener: OnItemRemoveClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_profile_nickname, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            tvItemFriendName.text = models[position].nickname

            try {
                storage.getReference(FirebaseConstants.STORAGE_PROFILE)
                    .child("${models[position].uid}.png").downloadUrl.addOnSuccessListener {
                    Glide.with(context).load(it).into(ivItemFriendProfile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemProfileNicknameBinding.bind(itemView)

        init {
            binding.ivFriendRemove.setOnClickListener {
                onItemRemoveClickListener.onItemRemoved(models[adapterPosition], adapterPosition)
            }
        }
    }
}