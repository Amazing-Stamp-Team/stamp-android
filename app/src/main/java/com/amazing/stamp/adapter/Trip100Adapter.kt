package com.amazing.stamp.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.KoreaTrip100
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFeedBinding
import com.example.stamp.databinding.ItemFeedImageBinding
import com.example.stamp.databinding.ItemTrip100Binding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Trip100Adapter(
    val context: Context,
    val trip100List: ArrayList<KoreaTrip100>
) :
    RecyclerView.Adapter<Trip100Adapter.Holder>() {

    private val storage by lazy { Firebase.storage }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_trip_100, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = trip100List[position]

            tvItemLocation.text = "${model.location1} ${model.location2}"
            tvItemTripTitle.text = model.name
            tvItemType.text = model.tag

            holder.itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(model.url))
                context.startActivity(intent)
            }

            try {
                storage.reference.child(FirebaseConstants.STORAGE_KOREA_TRIP_100)
                    .child("${model.name}.JPG").downloadUrl.addOnSuccessListener {
                    Glide.with(context).load(it).into(ivItemTrip)
                }
            } catch (e: Exception) {
                try {
                    storage.reference.child(FirebaseConstants.STORAGE_KOREA_TRIP_100)
                        .child("${model.name}.jpg").downloadUrl.addOnSuccessListener {
                        Glide.with(context).load(it).into(ivItemTrip)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return trip100List!!.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemTrip100Binding.bind(itemView)
    }
}