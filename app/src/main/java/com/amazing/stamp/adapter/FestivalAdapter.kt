package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.models.FestivalModel
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFestivalBinding
import java.text.SimpleDateFormat

class FestivalAdapter(val context: Context, private val festivalModels: ArrayList<FestivalModel>) :
    RecyclerView.Adapter<FestivalAdapter.Holder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var itemClickListener: OnItemClickListener? = null

    private val festivalSDF = SimpleDateFormat("yy.MM.dd")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_festival, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = festivalModels[position]

            Glide.with(context).load(context.getDrawable(R.drawable.ic_default_stamp))
                .into(ivItemFestival)
            tvItemFestivalTitle.text = model.title
            tvItemFestivalLocation.text = model.location
            tvItemFestivalCall.text = model.call

            var durationStr =
                if (model.durationStart != null) festivalSDF.format(model.durationStart.seconds * 1000)
                else ""

            durationStr += if (model.durationEnd != null) " ~ ${festivalSDF.format(model.durationEnd.seconds * 1000)}"
            else ""

            if (durationStr != null) {
                tvItemFestivalDuration.text = durationStr
            }
        }
    }

    override fun getItemCount(): Int {
        return festivalModels.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFestivalBinding.bind(itemView)

        init {
            itemView.setOnClickListener { itemClickListener?.onItemClick(bindingAdapterPosition) }
        }
    }
}