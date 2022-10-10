package com.amazing.stamp.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.MyPageTripModel
import com.example.stamp.R
import com.example.stamp.databinding.ItemMyPageTripBinding

class MyPageTripAdapter(
    val context: Context,
    val myPageTripModels: ArrayList<MyPageTripModel>,
) :
    RecyclerView.Adapter<MyPageTripAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_my_page_trip, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            tvItemTripTitle.text = myPageTripModels[position].title
            tvItemTripDay.text = myPageTripModels[position].day
            ivItemTripImage.clipToOutline = true
        }
    }

    override fun getItemCount(): Int {
        return myPageTripModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemMyPageTripBinding.bind(itemView)
    }
}