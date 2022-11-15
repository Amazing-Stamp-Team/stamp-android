package com.amazing.stamp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.PostModel
import com.amazing.stamp.utils.FirebaseConstants
import com.amazing.stamp.utils.Utils
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemMyPageTripBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MyPageTripAdapter(
    val context: Context,
    private val postIDs:ArrayList<String>,
    private val myPageTripModels: ArrayList<PostModel>
    //private val imageUri: String?
) :
    RecyclerView.Adapter<MyPageTripAdapter.Holder>() {

    interface OnItemClickListener { fun onItemClick(view: View, position: Int) }
    var onItemClickListener: OnItemClickListener? = null

    private val storage by lazy { Firebase.storage }
    private val auth: FirebaseAuth? = Firebase.auth
    val uid: String = auth!!.currentUser!!.uid
    val firestore = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_my_page_trip, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = myPageTripModels[position]
            val postID = postIDs[position]


            if (model.imageNames != null && model.imageNames.isNotEmpty()) {
                storage.getReference("${FirebaseConstants.STORAGE_POST}/$postID/${model.imageNames[0]}").downloadUrl.addOnSuccessListener {
                    Glide.with(context).load(it).into(ivItemTripImage)
                }
            }

            tvItemTripTitle.text = myPageTripModels[position].location
            tvItemTripDay.text = Utils.sliderDateFormat.format(myPageTripModels[position].startDate!!.toDate().time)
            ivItemTripImage.clipToOutline = true
        }
    }

    override fun getItemCount(): Int {
        return myPageTripModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemMyPageTripBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(it, bindingAdapterPosition)
            }
        }
    }
}