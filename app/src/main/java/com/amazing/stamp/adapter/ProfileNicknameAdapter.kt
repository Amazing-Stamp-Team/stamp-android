package com.amazing.stamp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.ProfileNicknameModel
import com.example.stamp.R
import com.example.stamp.databinding.ItemProfileNicknameBinding

class ProfileNicknameAdapter(
    val context: Context,
    val models: ArrayList<ProfileNicknameModel>,
) :
    RecyclerView.Adapter<ProfileNicknameAdapter.Holder>() {

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

            if (models[position].image != null) {
                val bmp = BitmapFactory.decodeByteArray(models[position].image, 0, models[position].image!!.size)
                ivItemFriendProfile.setImageBitmap(Bitmap.createScaledBitmap(bmp, 50, 50, false))
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