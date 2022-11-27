package com.amazing.stamp.adapter

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.amazing.stamp.api.dto.festivalDTO.Item
import com.amazing.stamp.models.FestivalModel
import com.amazing.stamp.utils.FirebaseConstants
import com.bumptech.glide.Glide
import com.example.stamp.R
import com.example.stamp.databinding.ItemFestivalBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat

class FestivalAdapter(
    private val context: Context,
    private val festivalModels: ArrayList<Item>
) :
    RecyclerView.Adapter<FestivalAdapter.Holder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    var itemClickListener: OnItemClickListener? = null

    private val dtoSDF = SimpleDateFormat("yyyyMMdd")
    private val festivalSDF = SimpleDateFormat("yyyy.MM.dd")
    private val storage = Firebase.storage

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_festival, parent, false)

        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.run {
            val model = festivalModels[position]

            // 뷰를 재사용하기 때문에 기존의 뷰가 남아있을 수 있음. 따라서 디폴트 이미지로 한번 더 세팅해야함
            Glide.with(context).load(context.getDrawable(R.drawable.ic_default_stamp)).into(ivItemFestival)

            try {
                Glide.with(context).load(model.firstimage).into(ivItemFestival)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            tvItemFestivalTitle.text = model.title
            tvItemFestivalLocation.text = model.addr1
            tvItemFestivalCall.text = "tel : ${model.tel}"
            try {
                tvItemFestivalDuration.text = "${festivalSDF.format(dtoSDF.parse(model.eventstartdate))} ~ ${festivalSDF.format(dtoSDF.parse(model.eventenddate))}"
            } catch (e: Exception) {
                e.printStackTrace()
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