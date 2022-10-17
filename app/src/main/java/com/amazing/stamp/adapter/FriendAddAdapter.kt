package com.amazing.stamp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.FriendAddModel
import com.example.stamp.R
import com.example.stamp.databinding.ItemFriendsAddBinding

class FriendAddAdapter(val context: Context, private val models: ArrayList<FriendAddModel>) :
    RecyclerView.Adapter<FriendAddAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_friends_add, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.run {
            binding.tvFriendsName.text = models[position].name
            binding.tvFriendsEmail.text = models[position].email

            if (models[position].image != null) {
                binding.ivFriendsProfile.setImageBitmap(models[position].image)
            }
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFriendsAddBinding.bind(itemView)
    }
}