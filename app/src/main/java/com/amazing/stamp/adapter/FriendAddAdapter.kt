package com.amazing.stamp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.UserModel
import com.amazing.stamp.utils.FirebaseConstants
import com.example.stamp.R
import com.example.stamp.databinding.ItemFriendsAddBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage

class FriendAddAdapter(
    val context: Context,
    private val storage: FirebaseStorage?,
    private val fireStore: FirebaseFirestore?,
    private val models: ArrayList<UserModel>
) :
    RecyclerView.Adapter<FriendAddAdapter.Holder>() {

    var idx = 0
    private var profileArrayList = ArrayList<ByteArray?>()

    interface ItemClickListener {
        fun onItemClick(profile:ByteArray?, userModel: UserModel)
    }

    lateinit var itemClickListener: ItemClickListener

    fun fireStoreSearch(keyword: String) {
        idx++

        fireStore?.collection(FirebaseConstants.COLLECTION_USERS)
            ?.addSnapshotListener { value, error ->
                models.clear()
                profileArrayList.clear()

                for (snapshot in value!!.documents) {
                    if (snapshot.getString(FirebaseConstants.USER_FIELD_NICKNAME)!!.contains(keyword)) {
                        val item = snapshot.toObject<UserModel>()
                        models.add(item!!)
                        profileArrayList.add(null)
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

            if (models[position].imageName != null && models[position].imageName != "") {
                setUserImage(binding, idx, models[position], position)
                //binding.ivFriendsProfile.setImageBitmap(models[position].image)
            } else {
                binding.ivFriendsProfile.setImageBitmap(null)
            }
        }
    }

    private fun setUserImage(binding: ItemFriendsAddBinding, idx:Int, userModel: UserModel, position: Int) {
        val gsReference = storage!!.getReference("${FirebaseConstants.STORAGE_PROFILE}/${userModel!!.imageName!!}")

        gsReference.getBytes(FirebaseConstants.TEN_MEGABYTE).addOnCompleteListener {
            if(it.isSuccessful) {
                if(idx == this.idx) {
                    val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result.size)
                    profileArrayList[position] = it.result

                    binding.ivFriendsProfile.setImageBitmap(
                        Bitmap.createScaledBitmap(
                            bmp,
                            binding.ivFriendsProfile.width,
                            binding.ivFriendsProfile.height,
                            false
                        )
                    )
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return models.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFriendsAddBinding.bind(itemView)
        init {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(profileArrayList[adapterPosition], models[adapterPosition])
            }
        }
    }
}