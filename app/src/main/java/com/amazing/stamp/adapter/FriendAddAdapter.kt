package com.amazing.stamp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.FriendAddModel
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.R
import com.example.stamp.databinding.ItemFriendsAddBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class FriendAddAdapter(val context: Context, val fireStore: FirebaseFirestore?, private val models: ArrayList<UserModel>) :
    RecyclerView.Adapter<FriendAddAdapter.Holder>() {

    fun fireStoreSearch(keyword:String) {
        fireStore?.collection(FirebaseConstants.COLLECTION_USERS)?.addSnapshotListener { value, error ->
            models.clear()

            for(snapshot in value!!.documents) {
                if(snapshot.getString(FirebaseConstants.USER_FIELD_NICKNAME)!!.contains(keyword)) {
                    val item = snapshot.toObject<UserModel>()
                    models.add(item!!)
                }
            }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_friends_add, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.run {
            binding.tvFriendsName.text = models[position].nickname
            binding.tvFriendsEmail.text = models[position].email

            if (models[position].imageName != null) {
                //binding.ivFriendsProfile.setImageBitmap(models[position].image)
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